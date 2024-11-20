package org.xuzhaorui.exception.pre;

/**
 * 预先认证异常
 */
public abstract class PreAuthenticationException extends RuntimeException{
    public PreAuthenticationException(String message) {
        super(message);
    }

    public PreAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
