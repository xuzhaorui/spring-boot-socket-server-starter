package org.xuzhaorui.exception.soc;

import org.springframework.util.Assert;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;

public class DefaultSocketNativeHandler implements SocketNativeHandler {


    private final SocketNativeEntryPoint socketNativeEntryPoint;

    public DefaultSocketNativeHandler(SocketNativeEntryPoint socketNativeEntryPoint) {
        Assert.notNull(socketNativeEntryPoint,"socketNativeEntryPoint is null");
        this.socketNativeEntryPoint = socketNativeEntryPoint;
    }

    @Override
    public  void handleSocketNative(SocketRequest request, SocketResponse response, SocketNativeException exception) throws IOException {
        socketNativeEntryPoint.commence(request,response,exception);
    }
}
