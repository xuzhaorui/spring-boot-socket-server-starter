package org.xuzhaorui.server;



import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;

public class SocketMetBean implements Serializable {

    private PrintWriter clientWriters ;
    private BufferedReader clientReaders;
    private Socket socket;

    /**
     * 用户认证信息与权限
     */
    private Object principal;
    public SocketMetBean(Socket socket, Object principal) {
        this.socket = socket;
        this.principal = principal;


    }

    public Object getPrincipal() {
        return principal;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public BufferedReader getClientReaders() {
        return clientReaders;
    }

    public void setClientReaders(BufferedReader clientReaders) {
        this.clientReaders = clientReaders;
    }

    public PrintWriter getClientWriters() {
        return clientWriters;
    }

    public void setClientWriters(PrintWriter clientWriters) {
        this.clientWriters = clientWriters;
    }
}
