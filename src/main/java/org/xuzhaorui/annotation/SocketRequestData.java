package org.xuzhaorui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Socket请求参数
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketRequestData {
    /**
     * 获取请求参数中的指定属性，支持嵌套属性，例如：属性.属性.属性
     * @return 指定属性值
     */
    String value() default "";
}

