package org.xuzhaorui.exception.auth;

import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;

/**
 * 入口点
 */
public interface SocketAuthenticationEntryPoint {
    void commence(SocketRequest request, SocketResponse response, SocketAuthenticationException exception)  throws IOException;
}
