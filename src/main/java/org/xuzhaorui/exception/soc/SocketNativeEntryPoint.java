package org.xuzhaorui.exception.soc;

import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;

public interface SocketNativeEntryPoint {
    void commence(SocketRequest request, SocketResponse response, SocketNativeException socketNativeException)  throws IOException;

}
