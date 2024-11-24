package org.xuzhaorui.autoconfigure;



import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.xuzhaorui.exception.SocketExceptionHandler;
import org.xuzhaorui.messageserialization.SocketMessageInterceptor;
import org.xuzhaorui.prefiltration.PreFiltrationProcessor;
import org.xuzhaorui.properties.SocketConfigProperties;
import org.xuzhaorui.readingmode.ReadWriteMode;
import org.xuzhaorui.readingmode.ReadingModeFactory;
import org.xuzhaorui.scanner.SocketMappingHandler;
import org.xuzhaorui.server.SocketServer;
import org.xuzhaorui.store.SocketAuthenticationSuccessfulStorage;
import org.xuzhaorui.store.FilterChainRegistry;
import org.xuzhaorui.store.SocketMessageInfoRegistry;
import org.xuzhaorui.store.SocketMethodMappingRegistry;
import org.xuzhaorui.url.AllowedUrlManager;
import org.xuzhaorui.url.DefAllowedUrlManager;
import org.xuzhaorui.utils.ValidUtil;

import javax.validation.Validator;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Socket 服务器自动配置
 */
@Configuration
@ConditionalOnProperty(prefix = "socket.server", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SocketConfigProperties.class)
@Import({
        SocketExceptionAutoConfiguration.class,
        SocketMappingConfiguration.class,
        SocketMessageSerializationConfiguration.class,SocketFilterConfiguration.class
})
public class SocketServerAutoConfiguration {
    /**
     * Socket 服务器
     * @param socketThreadPool  线程池
     * @param socketConfigProperties 配置
     * @param socketMappingHandler 映射处理器
     * @param allowedUrlManager 允许的url
     * @param exceptionHandler 异常处理器
     * @param preFiltrationProcessor 预处理
     * @param readWriteMode 消息读写模式
     * @param filterChainRegistry 过滤器链
     * @return SocketServer
     */
    @Bean
    public SocketServer socketServer(
            ThreadPoolTaskExecutor socketThreadPool,
                                     SocketConfigProperties socketConfigProperties,
                                     SocketMappingHandler socketMappingHandler,
                                     AllowedUrlManager allowedUrlManager,
                                     SocketExceptionHandler exceptionHandler,
                                PreFiltrationProcessor preFiltrationProcessor,
                                ReadWriteMode readWriteMode,
                                FilterChainRegistry filterChainRegistry


    )
    {
      return new SocketServer(
              socketConfigProperties,socketMappingHandler,allowedUrlManager,exceptionHandler,filterChainRegistry, readWriteMode,preFiltrationProcessor,socketThreadPool
      );
    }

    /**
     * Socket消息拦截器
     * @return SocketMessageInterceptor
     */
    @Bean
    public SocketMessageInterceptor socketMessageInterceptor(){
        return new SocketMessageInterceptor();
    }


    /**
     * PreFiltrationProcessor
     * @param socketMessageInterceptor 消息拦截器
     * @param validUtil 验证工具
     * @param socketMethodMappingRegistry Socket请求url映射表
     * @param socketMessageInfoRegistry  socket传输消息序列化注册表
     * @return PreFiltrationProcessor
     */
    @Bean
    public PreFiltrationProcessor preFiltrationProcessor(SocketMessageInterceptor socketMessageInterceptor, ValidUtil validUtil, SocketMethodMappingRegistry socketMethodMappingRegistry, SocketMessageInfoRegistry socketMessageInfoRegistry) {
        return new PreFiltrationProcessor(socketMessageInterceptor,validUtil,socketMethodMappingRegistry,socketMessageInfoRegistry);
    }

    /**
     * 验证工具
     * @param validator  validator
     * @return ValidUtil
     */
    @Bean
    public ValidUtil validUtil(Validator validator) {
        return new ValidUtil(validator);
    }


    /**
     * 消息读写模式
     * @param socketConfigProperties Socket 配置属性
     * @return  ReadWriteMode
     */
    @Bean
    public ReadWriteMode readingMode(SocketConfigProperties socketConfigProperties) {
        String readingMode = socketConfigProperties.getReadingMode();
        if (readingMode == null) readingMode = "line";
        return ReadingModeFactory.createReadingMode(readingMode);
    }

    /**
     * socket认证成功存储
     * @return SocketAuthenticationSuccessfulStorage
     */
    @Bean
    public SocketAuthenticationSuccessfulStorage socketAuthenticationSuccessfulStorage() {
        return new SocketAuthenticationSuccessfulStorage();
    }


    /**
     * 允许的 url 管理器
     * @return DefAllowedUrlManager
     */
    @Bean
    @ConditionalOnMissingBean(AllowedUrlManager.class)
    public DefAllowedUrlManager defAllowedUrlManager() {
        DefAllowedUrlManager defAllowedUrlManager = new DefAllowedUrlManager();
        defAllowedUrlManager.addAllowedUrl("/login");
        return defAllowedUrlManager;
    }



    /**
     * socket线程池
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "socketThreadPool")
    public ThreadPoolTaskExecutor socketThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2; // 核心线程数根据 CPU 核心数调整
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(200); // 保留最大值为200
        executor.setQueueCapacity(500); // 根据实际场景缩小队列大小
        executor.setKeepAliveSeconds(180); // 线程空闲时间调整为180秒
        executor.setThreadNamePrefix("SkThreadPool-");

        executor.setThreadFactory(new SocketServerAutoConfiguration.CustomThreadFactory());

        // 拒绝策略保持为CallerRunsPolicy
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
            try {
                if (!executor.getThreadPoolExecutor().awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.getThreadPoolExecutor().shutdownNow();
                }
            } catch (InterruptedException ex) {
                executor.getThreadPoolExecutor().shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));

        return executor;
    }


    /**
     * 自定义线程工厂，为线程命名，便于问题排查
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        public CustomThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, "SkThreadPool-" + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
