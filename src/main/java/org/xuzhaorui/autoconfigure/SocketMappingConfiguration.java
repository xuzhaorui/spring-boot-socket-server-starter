package org.xuzhaorui.autoconfigure;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xuzhaorui.store.SocketMethodMappingRegistry;
import org.xuzhaorui.scanner.SocketControllerScanner;
import org.xuzhaorui.scanner.SocketMappingHandler;


@Configuration
public class SocketMappingConfiguration {



    /**
     * Socket请求url映射表
     * @return SocketMethodMappingRegistry
     */
    @Bean
    public SocketMethodMappingRegistry socketMethodMappingRegistry() {
        return new SocketMethodMappingRegistry();
    }
    /**
     * 映射处理程序
     * @param socketMethodMappingRegistry  Socket请求url映射表
     * @return SocketMappingHandler
     */
    @Bean
    public SocketMappingHandler socketMappingHandler(SocketMethodMappingRegistry socketMethodMappingRegistry){
        return new SocketMappingHandler(socketMethodMappingRegistry);
    }

    /**
     * 请求url映射扫描器
     * @param socketMappingRegistry Socket请求url映射表
     * @param applicationContext 上下文
     * @return SocketControllerScanner
     */
    @Bean
    public SocketControllerScanner socketControllerScanner(SocketMethodMappingRegistry socketMappingRegistry, ApplicationContext applicationContext) {
        return new SocketControllerScanner(socketMappingRegistry,applicationContext);
    }

}
