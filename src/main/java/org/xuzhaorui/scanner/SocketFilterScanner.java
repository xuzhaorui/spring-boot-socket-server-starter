package org.xuzhaorui.scanner;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.xuzhaorui.annotation.FilterChain;
import org.xuzhaorui.filter.SocketFilter;
import org.xuzhaorui.store.FilterChainRegistry;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * 过滤链扫描器
 */
public class SocketFilterScanner {
    private final FilterChainRegistry filterChainRegistry;
    private final ApplicationContext applicationContext;

    public SocketFilterScanner(FilterChainRegistry filterChainRegistry, ApplicationContext applicationContext) {
        this.filterChainRegistry = filterChainRegistry;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void scanSocketFilters() {
        // 获取所有类型为 SocketFilter 的 Bean
        Map<String, SocketFilter> socketFilters = applicationContext.getBeansOfType(SocketFilter.class);

        for (SocketFilter filter : socketFilters.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(filter);
            if (targetClass.isAnnotationPresent(FilterChain.class)) {
                FilterChain filterChainAnnotation = targetClass.getAnnotation(FilterChain.class);
                String url = filterChainAnnotation.url();
                filterChainRegistry.register(url, filter);
            }
        }
    }
}
