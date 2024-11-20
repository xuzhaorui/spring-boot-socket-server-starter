package org.xuzhaorui.exception.auth;

// 网络连接异常
public class SocketConnectionException extends SocketAuthenticationException {
    public SocketConnectionException(String message) {
        super(message);
    }
}
