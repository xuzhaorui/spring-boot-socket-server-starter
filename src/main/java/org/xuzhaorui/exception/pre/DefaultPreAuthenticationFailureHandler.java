package org.xuzhaorui.exception.pre;

import org.springframework.util.Assert;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;

public class DefaultPreAuthenticationFailureHandler implements PreAuthenticationFailureHandler{
    private final PreAuthenticationEntryPoint preAuthenticationEntryPoint;

    public DefaultPreAuthenticationFailureHandler(PreAuthenticationEntryPoint preAuthenticationEntryPoint) {
        Assert.notNull(preAuthenticationEntryPoint, "preAuthenticationEntryPoint cannot be null");
        this.preAuthenticationEntryPoint = preAuthenticationEntryPoint;
    }

    @Override
    public void handlePreAuthenticationFailure(SocketRequest request, SocketResponse response, PreAuthenticationException exception) throws IOException {
        preAuthenticationEntryPoint.commence(request,response,exception);
    }
}
