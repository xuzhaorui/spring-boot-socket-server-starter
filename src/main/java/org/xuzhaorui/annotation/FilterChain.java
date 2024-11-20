package org.xuzhaorui.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.xuzhaorui.filter.SocketFilterChain;

import java.lang.annotation.*;

/**
 * 用于标记过滤链
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface FilterChain {

    @AliasFor(
            annotation = Component.class
    )
    String value() default "";

    /**
     * 过滤连所过滤的url ,例如：“/recipeIssued/**”
     *  @return  url
     */
    String url() default SocketFilterChain.DEFAULT_FILTER_CHAIN;


    /**
     * 过滤链的执行顺序
     * 参考org.xuzhaorui.pojo.PathMatcher中的main方法
     * @return  order
     */
    int order() default Integer.MIN_VALUE;
}
