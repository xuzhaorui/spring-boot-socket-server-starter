package org.xuzhaorui.exception.pre;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;

public class DefaultPreAuthenticationEntryPoint implements PreAuthenticationEntryPoint{

    private static final Logger log = LoggerFactory.getLogger(DefaultPreAuthenticationEntryPoint.class);

    @Override
    public void commence(SocketRequest request, SocketResponse response, PreAuthenticationException exception) throws IOException {
//        if (exception instanceof MessageSerializationException) {
//            log.warn("MessageSerializationException");
//            response.setCode(301);
//            response.setMessage("MessageSerializationException：" + exception.getMessage());
//        }
         if (exception instanceof SocketMappingException) {
            response.setCode(301);
            response.setMessage("SocketMappingException：" + exception.getMessage());
            log.warn("SocketMappingException");
        }else if (exception instanceof SocketValidationException) {
            response.setCode(302);
            response.setMessage("SocketValidationException：" + exception.getMessage());
            log.warn("SocketValidationException");
        }else if (exception instanceof ParsingException) {
            response.setCode(303);
            response.setMessage("ParsingException：" + exception.getMessage());
            log.warn("UrlParsingException");
        } else {
            response.setCode(304);
            response.setMessage("Authentication failed due to unknown reason.：" + exception.getMessage());
            log.warn("Authentication failed due to unknown reason.");

        }
    }
}
