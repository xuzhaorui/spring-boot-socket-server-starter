package org.xuzhaorui.exception.auth;

import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;

/**
 * 身份验证失败处理程序
 */
public interface SocketAuthenticationFailureHandler {
    void handleAuthenticationFailure(SocketRequest request, SocketResponse response, SocketAuthenticationException exception) throws IOException;
}
