package org.xuzhaorui.exception.soc;

import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;

import java.io.IOException;

/**
 * 超时
 */
public interface SocketNativeHandler {
    void handleSocketNative(SocketRequest request, SocketResponse response, SocketNativeException exception) throws IOException;
}
