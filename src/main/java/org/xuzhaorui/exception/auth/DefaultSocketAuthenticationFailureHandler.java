package org.xuzhaorui.exception.auth;

import org.springframework.util.Assert;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;

public class DefaultSocketAuthenticationFailureHandler implements SocketAuthenticationFailureHandler {

    private final SocketAuthenticationEntryPoint socketAuthenticationEntryPoint;

    public DefaultSocketAuthenticationFailureHandler(SocketAuthenticationEntryPoint socketAuthenticationEntryPoint) {
        Assert.notNull(socketAuthenticationEntryPoint, "socketAuthenticationEntryPoint cannot be null");
        this.socketAuthenticationEntryPoint = socketAuthenticationEntryPoint;
    }

    @Override
    public void handleAuthenticationFailure(SocketRequest request, SocketResponse response, SocketAuthenticationException exception) throws IOException {
         socketAuthenticationEntryPoint.commence(request,response,exception);
    }
}
