package org.xuzhaorui.exception.auth;


// 认证基础异常
public abstract class SocketAuthenticationException extends RuntimeException {
    public SocketAuthenticationException(String message) {
        super(message);
    }

    public SocketAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}








