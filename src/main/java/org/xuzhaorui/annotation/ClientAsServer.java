package org.xuzhaorui.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在标记SocketMessage注解的bean中的属性，代表本次消息为客户端作为服务端响应
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientAsServer {
}
