package org.xuzhaorui.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xuzhaorui.store.SocketMessageInfoRegistry;
import org.xuzhaorui.properties.SocketConfigProperties;
import org.xuzhaorui.scanner.SocketMessageScanner;

@Configuration
public class SocketMessageSerializationConfiguration {

    /**
     * socket传输消息序列化注册表
     * @return SocketMessageInfoRegistry
     */
    @Bean
    public SocketMessageInfoRegistry socketMessageInfoRegistry() {
        return new SocketMessageInfoRegistry();
    }



    /**
     * 消息扫描器
     * @param socketMessageInfoRegistry socket传输消息序列化注册表
     * @param socketConfigProperties  socket配置
     * @return SocketMessageScanner
     */
    @Bean
    public SocketMessageScanner socketMessageScanner(SocketMessageInfoRegistry socketMessageInfoRegistry, SocketConfigProperties socketConfigProperties){
        return new SocketMessageScanner(socketMessageInfoRegistry, socketConfigProperties);
    }

}
