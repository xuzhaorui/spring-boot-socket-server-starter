package org.xuzhaorui.exception;

import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

/**
 * 授权处理
 */
public interface SocketAccessDeniedHandler {
    void handleAccessDenied(SocketRequest request, SocketResponse response, SocketAccessDeniedException exception);
}
