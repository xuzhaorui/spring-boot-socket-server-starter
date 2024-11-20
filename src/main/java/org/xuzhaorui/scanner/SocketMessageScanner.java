package org.xuzhaorui.scanner;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.xuzhaorui.annotation.SocketCode;
import org.xuzhaorui.annotation.SocketMessage;
import org.xuzhaorui.annotation.SocketMsg;
import org.xuzhaorui.annotation.SocketUrl;
import org.xuzhaorui.messageserialization.SocketMessageSerializer;
import org.xuzhaorui.store.SocketMessageInfo;
import org.xuzhaorui.store.SocketMessageInfoRegistry;
import org.xuzhaorui.properties.SocketConfigProperties;
import org.xuzhaorui.utils.SocketUrlFinder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 用于扫描SocketMessage的消息扫描器
 */

public class SocketMessageScanner {

    private static final Logger log = LoggerFactory.getLogger(SocketMessageScanner.class);
    private final SocketMessageInfoRegistry socketMessageInfoRegistry;
    private final SocketConfigProperties socketConfigProperties;

    public SocketMessageScanner(SocketMessageInfoRegistry socketMessageInfoRegistry, SocketConfigProperties socketConfigProperties) {
        this.socketMessageInfoRegistry = socketMessageInfoRegistry;
        this.socketConfigProperties = socketConfigProperties;
    }


    @PostConstruct
    public void scanSocketMessages() throws ClassNotFoundException {
        // 创建扫描器
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);

        // 添加过滤器，查找带有 @SocketMessage 注解的类
        scanner.addIncludeFilter(new AnnotationTypeFilter(SocketMessage.class));

        // 扫描指定包中的类
        String scanSocketMessagePackage = socketConfigProperties.getScanSocketMessagePackage();
        Assert.hasText(scanSocketMessagePackage, "scanSocketMessagePackage 不能为空");
        // 如果未配置扫描路径，设置为全局扫描
        // 全局扫描的性能：
        // 全局扫描可能会带来性能开销，特别是在大规模项目中。因此建议设置明确的扫描路径，避免不必要的组件被加载。
        if (!StringUtils.hasText(scanSocketMessagePackage)) {
            log.warn("scanSocketMessagePackage 未设置，默认全局扫描");
            scanSocketMessagePackage = ""; // 设置为空字符串表示全局扫描
        }


        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(scanSocketMessagePackage);
        // 验证是否找到候选组件
        if (beanDefinitions.isEmpty()) {
            throw new IllegalArgumentException("未在路径 '" + scanSocketMessagePackage + "' 中找到任何标记@SocketMessage的类");
        }

        for (BeanDefinition beanDefinition : beanDefinitions) {
            // 获取类对象
            Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());

            // 获取类上的 @SocketMessage 注解
            SocketMessage socketMessageAnnotation = clazz.getAnnotation(SocketMessage.class);

            if (socketMessageAnnotation != null) {
                // 将类和注解信息存储到 SocketConfig 中
                SocketMessageInfo messageInfo = new SocketMessageInfo(
                        socketMessageAnnotation.priority()
                );
                // 查找 @SocketUrl 注解字段与层级关系
                List<String> socketUrlPaths = SocketUrlFinder.fieldLookingForMarkupAnnotationsCanBeEmbedded(clazz, SocketUrl.class);
                messageInfo.setSocketUrlPaths(socketUrlPaths);

                // 查找 @SocketCode 注解字段与层级关系
                List<String> socketCodePaths = SocketUrlFinder.fieldLookingForMarkupAnnotationsCanBeEmbedded(clazz, SocketCode.class);
                messageInfo.setSocketCodePaths(socketCodePaths);

                // 查找 @SocketMsg 注解字段与层级关系
                List<String> socketMsgPaths = SocketUrlFinder.fieldLookingForMarkupAnnotationsCanBeEmbedded(clazz, SocketMsg.class);
                messageInfo.setSocketMsgPaths(socketMsgPaths);

                // 获取该类的自定义序列化器，并缓存到serializers

                Class<? extends SocketMessageSerializer<?,?>> serializerClass = socketMessageAnnotation.serializer();
                try {
                    // 实例化用户自定义的序列化器
                    SocketMessageSerializer<?,?> serializer = serializerClass.getDeclaredConstructor().newInstance();
                    messageInfo.setSocketMessageSerializer(serializer);
                } catch (Exception e) {
                    throw new RuntimeException("Error instantiating serializer", e);
                }

                socketMessageInfoRegistry.addSocketMessage(clazz, messageInfo);
            }
        }
    }


}
