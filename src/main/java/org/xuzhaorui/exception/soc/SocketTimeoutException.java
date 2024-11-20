package org.xuzhaorui.exception.soc;

// 认证超时异常
public class SocketTimeoutException extends SocketNativeException {
    public SocketTimeoutException(String message) {
        super(message);
    }
}
