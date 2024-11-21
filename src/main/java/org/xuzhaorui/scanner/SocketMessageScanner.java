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
import java.util.concurrent.ConcurrentHashMap;

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

        List<SocketMessageSerializer> socketMessageSerializers = new ArrayList<>();
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


                initSerializerClassArray(socketMessageAnnotation, socketMessageSerializers, messageInfo);

                socketMessageInfoRegistry.addSocketMessage(clazz, messageInfo);
            }
        }
        if (socketConfigProperties.isPrintSerializerMemoryAddress()){
            ConcurrentHashMap<Class<?>, SocketMessageInfo> socketMessages = socketMessageInfoRegistry.getSocketMessages();

            socketMessages.values().stream()
                    .flatMap(socketMessageInfo -> socketMessageInfo.getSocketMessageSerializerList().stream())
                    .forEach(socketMessageSerializer -> log.info("序列化器内存地址：{}", socketMessageSerializer.hashCode()));
        }

    }

    private static void initSerializerClassArray(SocketMessage socketMessageAnnotation,
                                                 List<SocketMessageSerializer> socketMessageSerializers,
                                                 SocketMessageInfo messageInfo) {
        Class<? extends SocketMessageSerializer>[] serializerClassArray = socketMessageAnnotation.serializer();

        try {
            for (Class<? extends SocketMessageSerializer> serializerClass : serializerClassArray) {
                // 查找缓存中是否已有该类的序列化器实例
                Optional<SocketMessageSerializer> cachedSerializer = socketMessageSerializers.stream()
                        .filter(serializer -> serializer.getClass().equals(serializerClass))
                        .findFirst();

                SocketMessageSerializer serializer;

                // 如果缓存中已存在，直接使用缓存的实例
                if (cachedSerializer.isPresent()) {
                    serializer = cachedSerializer.get();
                } else {
                    // 如果不存在缓存，创建新的实例并缓存
                    serializer = serializerClass.getDeclaredConstructor().newInstance();
                    socketMessageSerializers.add(serializer);
                }

                // 如果 messageInfo 中未设置序列化器列表，初始化
                if (messageInfo.getSocketMessageSerializerList() == null) {
                    messageInfo.setSocketMessageSerializerList(new ArrayList<>());
                }

                // 将序列化器加入 messageInfo
                messageInfo.getSocketMessageSerializerList().add(serializer);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error instantiating serializer", e);
        }
    }



}
