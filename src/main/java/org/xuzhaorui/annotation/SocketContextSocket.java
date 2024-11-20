package org.xuzhaorui.annotation;

import java.lang.annotation.*;

/**
 * 用于获取请求中的socket
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketContextSocket {

}
