package org.xuzhaorui.exception.pre;

/**
 * SocketValidationException 验证异常
 */
public class SocketValidationException extends PreAuthenticationException {

    public SocketValidationException(String message) {
        super(message);
    }

    public SocketValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
