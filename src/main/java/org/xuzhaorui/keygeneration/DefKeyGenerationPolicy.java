package org.xuzhaorui.keygeneration;

import org.xuzhaorui.filter.SocketRequest;

import java.net.Socket;

public class DefKeyGenerationPolicy implements KeyGenerationPolicy {
    @Override
    public String generationKey(SocketRequest request) {
        // 默认IP加端口
        Socket clientSocket = request.getClientSocket();
        return clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
    }
}
