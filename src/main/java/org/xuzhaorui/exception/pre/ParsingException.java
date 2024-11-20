package org.xuzhaorui.exception.pre;

/**
 *  解析异常
 */
public class ParsingException extends PreAuthenticationException {

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
