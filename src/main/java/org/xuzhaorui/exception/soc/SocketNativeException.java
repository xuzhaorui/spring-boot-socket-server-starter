package org.xuzhaorui.exception.soc;

// 顶层异常基类
public abstract class SocketNativeException extends RuntimeException {

    public SocketNativeException(String message) {
        super(message);
    }

    public SocketNativeException(String message, Throwable cause) {
        super(message, cause);
    }
}


