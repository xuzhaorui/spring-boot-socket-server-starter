package org.xuzhaorui.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xuzhaorui.exception.SocketExceptionHandler;
import org.xuzhaorui.exception.auth.DefaultSocketAuthenticationEntryPoint;
import org.xuzhaorui.exception.auth.DefaultSocketAuthenticationFailureHandler;
import org.xuzhaorui.exception.auth.SocketAuthenticationEntryPoint;
import org.xuzhaorui.exception.auth.SocketAuthenticationFailureHandler;
import org.xuzhaorui.exception.pre.DefaultPreAuthenticationEntryPoint;
import org.xuzhaorui.exception.pre.DefaultPreAuthenticationFailureHandler;
import org.xuzhaorui.exception.pre.PreAuthenticationEntryPoint;
import org.xuzhaorui.exception.pre.PreAuthenticationFailureHandler;
import org.xuzhaorui.exception.soc.DefaultSocketNativeEntryPoint;
import org.xuzhaorui.exception.soc.DefaultSocketNativeHandler;
import org.xuzhaorui.exception.soc.SocketNativeEntryPoint;
import org.xuzhaorui.exception.soc.SocketNativeHandler;
import org.xuzhaorui.store.SocketMessageInfoRegistry;
import org.xuzhaorui.properties.SocketConfigProperties;
import org.xuzhaorui.readingmode.ReadWriteMode;

/**
 * 异常自动配置
 */
@Configuration
public class SocketExceptionAutoConfiguration {


    /**
     *
     * @param readWriteMode 读取消息模式
     * @param socketConfigProperties 配置
     * @param failureHandler 认证失败异常处理
     * @param preFailureHandler 预先认证异常处理
     * @param socketNativeHandler socket原生异常处理
     * @param socketMessageInfoRegistry 消息信息注册
     * @return 异常处理
     */
    @Bean
    public SocketExceptionHandler socketExceptionHandler(
            ReadWriteMode readWriteMode,
            SocketConfigProperties socketConfigProperties,
            SocketAuthenticationFailureHandler failureHandler,
            PreAuthenticationFailureHandler preFailureHandler,
            SocketNativeHandler socketNativeHandler,
            SocketMessageInfoRegistry socketMessageInfoRegistry
            ){
            return new SocketExceptionHandler(
                    readWriteMode,socketConfigProperties,
                    failureHandler,preFailureHandler,socketNativeHandler,socketMessageInfoRegistry
            );
    }

    /**
     * 认证异常
     * @return SocketAuthenticationEntryPoint
     */
    @Bean
    @ConditionalOnMissingBean(SocketAuthenticationEntryPoint.class)
    public SocketAuthenticationEntryPoint socketAuthenticationEntryPoint(){
        return new DefaultSocketAuthenticationEntryPoint();
    }

    /**
     * 认证失败
     * @param socketAuthenticationEntryPoint 认证入口点
     * @return SocketAuthenticationFailureHandler
     */
    @Bean
    @ConditionalOnMissingBean(SocketAuthenticationFailureHandler.class)
    public SocketAuthenticationFailureHandler socketAuthenticationFailureHandler(SocketAuthenticationEntryPoint socketAuthenticationEntryPoint){
        return new DefaultSocketAuthenticationFailureHandler(socketAuthenticationEntryPoint);
    }
    /**
     * 预认证异常
     * @return PreAuthenticationEntryPoint
     */
    @Bean
    @ConditionalOnMissingBean(PreAuthenticationEntryPoint.class)
    public PreAuthenticationEntryPoint preAuthenticationEntryPoint(){
        return new DefaultPreAuthenticationEntryPoint();
    }

    /**
     *
     * @param preAuthenticationEntryPoint 入口
     * @return PreAuthenticationFailureHandler
     */
    @Bean
    @ConditionalOnMissingBean(PreAuthenticationFailureHandler.class)
    public PreAuthenticationFailureHandler preAuthenticationFailureHandler(PreAuthenticationEntryPoint preAuthenticationEntryPoint){
        return new DefaultPreAuthenticationFailureHandler(preAuthenticationEntryPoint);
    }
    /**
     * SocketNative
     * @return SocketNativeEntryPoint
     */
    @Bean
    @ConditionalOnMissingBean(SocketNativeEntryPoint.class)
    public SocketNativeEntryPoint socketNativeEntryPoint(){
        return new DefaultSocketNativeEntryPoint();
    }

    /**
     *
     * @param socketNativeEntryPoint 入口
     * @return SocketNativeHandler
     */
    @Bean
    @ConditionalOnMissingBean(SocketNativeHandler.class)
    public SocketNativeHandler socketNativeHandler(SocketNativeEntryPoint socketNativeEntryPoint){
        return new DefaultSocketNativeHandler(socketNativeEntryPoint);
    }


}
