package org.xuzhaorui.exception.pre;

public class MessageSerializationException extends RuntimeException{
    public MessageSerializationException(String message) {
        super(message);
    }

    public MessageSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
