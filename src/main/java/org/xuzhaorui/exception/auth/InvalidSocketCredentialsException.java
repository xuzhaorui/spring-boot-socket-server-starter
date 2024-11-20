package org.xuzhaorui.exception.auth;

// 无效的认证凭证异常
public class InvalidSocketCredentialsException extends SocketAuthenticationException {
    public InvalidSocketCredentialsException(String message) {
        super(message);
    }

    public InvalidSocketCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
