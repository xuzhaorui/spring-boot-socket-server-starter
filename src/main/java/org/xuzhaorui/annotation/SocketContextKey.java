package org.xuzhaorui.annotation;


import java.lang.annotation.*;

/**
 * 用于获取请求的Socket的key
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketContextKey {
}

