package org.xuzhaorui.exception.pre;

import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;

public interface PreAuthenticationFailureHandler {
    void handlePreAuthenticationFailure(SocketRequest request, SocketResponse response, PreAuthenticationException exception) throws IOException;
}
