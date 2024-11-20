package org.xuzhaorui.exception.soc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;
public class DefaultSocketNativeEntryPoint implements SocketNativeEntryPoint{
    private static final Logger log = LoggerFactory.getLogger(DefaultSocketNativeEntryPoint.class);

    @Override
    public  void commence(SocketRequest request, SocketResponse response, SocketNativeException exception) throws IOException {
        if (exception instanceof SocketTimeoutException) {
            log.warn("SocketTimeoutException request timed out.");
            response.setCode(501);
            response.setMessage("SocketTimeoutException request timed out" + exception.getMessage());
        }  else {
            response.setCode(501);
            response.setMessage("SocketNativeException:" + exception.getMessage());
            log.warn("SocketNativeException failed due to unknown reason.");
        }
    }
}
