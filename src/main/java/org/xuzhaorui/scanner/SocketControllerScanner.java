package org.xuzhaorui.scanner;



import org.xuzhaorui.annotation.SocketController;
import org.xuzhaorui.annotation.SocketMapping;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.xuzhaorui.store.SocketMethodMappingRegistry;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class SocketControllerScanner {
    private final SocketMethodMappingRegistry socketMappingRegistry;
    private final ApplicationContext applicationContext;

    public SocketControllerScanner(SocketMethodMappingRegistry socketMappingRegistry, ApplicationContext applicationContext) {
        this.socketMappingRegistry = socketMappingRegistry;
        this.applicationContext = applicationContext;
    }
    @PostConstruct
    public void scanSocketControllers() {
        // 从Spring容器中获取所有标注了 @SocketController 的 Bean
        Map<String, Object> socketControllers = applicationContext.getBeansWithAnnotation(SocketController.class);

        for (Object controller : socketControllers.values()) {
            // 获取原始类，而不是代理类
            Class<?> targetClass = AopUtils.getTargetClass(controller);
            Method[] methods = targetClass.getDeclaredMethods();

            for (Method method : methods) {
                if (method.isAnnotationPresent(SocketMapping.class)) {
                    SocketMapping socketMapping = method.getAnnotation(SocketMapping.class);
                    String mappingUrl = socketMapping.value();
                    //获取方法上的参数与对应的注解
                    Parameter[] parameters = method.getParameters();
                    // 注册到Socket请求映射表，controller是Spring管理的Bean，不需要反射
                    socketMappingRegistry.registerMapping(mappingUrl, new SocketHandlerMethod(controller, method,parameters));
                }
            }
        }
    }

}
