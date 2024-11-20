package org.xuzhaorui.annotation;

import java.lang.annotation.*;

/**
 * 用于标记SocketMessage的code字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketCode {
}
