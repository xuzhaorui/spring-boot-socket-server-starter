package org.xuzhaorui.annotation;

import java.lang.annotation.*;

/**
 * 标注在方法上，类似与@PostMapping，根据标注SocketUrl的属性定位
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketMapping {
    String value();
}


