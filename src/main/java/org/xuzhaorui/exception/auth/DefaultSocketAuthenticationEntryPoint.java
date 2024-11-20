package org.xuzhaorui.exception.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;
public class DefaultSocketAuthenticationEntryPoint implements SocketAuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(DefaultSocketAuthenticationEntryPoint.class);

    @Override
    public void commence(SocketRequest request, SocketResponse response, SocketAuthenticationException exception)  throws IOException {
        if (exception instanceof InvalidSocketCredentialsException) {
            log.warn("Invalid credentials provided.");
            response.setCode(401);
            response.setMessage("Invalid credentials provided.");
        }else if (exception instanceof SocketConnectionException) {
            response.setCode(402);
            response.setMessage("Network connection error occurred.");
            log.warn("Network connection error occurred.");
        } else {
            response.setCode(403);
            response.setMessage("Authentication failed due to unknown reason.");
            log.warn("Authentication failed due to unknown reason.");
        }
    }
}
