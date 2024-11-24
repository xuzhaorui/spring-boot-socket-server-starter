package org.xuzhaorui.server;

import java.net.Socket;

public class ClientConnectionInfo {

    private final Socket socket;
    /**
     *
     * 用户认证信息与权限
     */
    private final Object principal;

    public ClientConnectionInfo(Socket socket, Object principal) {
        this.socket = socket;
        this.principal = principal;
    }

    public Socket getSocket() {
        return socket;
    }

    public Object getPrincipal() {
        return principal;
    }


}
