package org.xuzhaorui.exception.auth;

public class SocketAuthenticationFailException extends SocketAuthenticationException{
    public SocketAuthenticationFailException(String message) {
        super(message);
    }

    public SocketAuthenticationFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
