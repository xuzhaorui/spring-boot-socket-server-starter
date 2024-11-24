package org.xuzhaorui.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientConnectionInfo {
    private  String clientId;  // 或者存储 IP 地址和端口
    private final InetAddress clientAddress;
    private final int clientPort;
    /**
     * 用户认证信息与权限
     */
    private Object principal;
    public ClientConnectionInfo( InetAddress clientAddress, int clientPort) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    public Object getPrincipal() {
        return principal;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    // 需要发送消息时可以通过 Socket 重新建立连接
    public Socket createSocket() throws IOException {
        return new Socket(clientAddress, clientPort);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }
}
