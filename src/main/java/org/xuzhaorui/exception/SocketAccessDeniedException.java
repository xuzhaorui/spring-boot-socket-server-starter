package org.xuzhaorui.exception;

// 授权失败异常
// TOdo
public class SocketAccessDeniedException extends RuntimeException {
    public SocketAccessDeniedException(String message) {
        super(message);
    }
}
