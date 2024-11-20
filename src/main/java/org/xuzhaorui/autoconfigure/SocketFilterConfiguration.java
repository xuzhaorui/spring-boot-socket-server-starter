package org.xuzhaorui.autoconfigure;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xuzhaorui.filter.AuthenticationFilter;
import org.xuzhaorui.store.FilterChainRegistry;
import org.xuzhaorui.scanner.SocketFilterScanner;
import org.xuzhaorui.store.SocketAuthenticationSuccessfulStorage;

/**
 * SocketFilter配置
 */
@Configuration
public class SocketFilterConfiguration {
    /**
     * 过滤链注册表
     * @return FilterChainRegistry
     */
    @Bean
    public FilterChainRegistry filterChainRegistry() {
        return new FilterChainRegistry();
    }

    /**
     *
     * @param socketAuthenticationSuccessfulStorage 存储
     * @return AuthenticationFilter
     */
    @Bean
    public AuthenticationFilter authenticationFilter(SocketAuthenticationSuccessfulStorage socketAuthenticationSuccessfulStorage) {
        return new AuthenticationFilter(socketAuthenticationSuccessfulStorage);
    }

    /**
     *
     * @param filterChainRegistry  filterChainRegistry
     * @param applicationContext applicationContext
     * @return SocketFilterScanner
     */
    @Bean
    public SocketFilterScanner socketFilterScanner(FilterChainRegistry filterChainRegistry, ApplicationContext applicationContext) {
        return new SocketFilterScanner(filterChainRegistry,applicationContext);
    }

}
