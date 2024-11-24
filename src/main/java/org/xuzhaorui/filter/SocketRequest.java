package org.xuzhaorui.filter;


import java.net.InetAddress;
import java.net.Socket;


public class SocketRequest {

    /**
     * 请求url
     */
    private  String requestUrl;

    /**
     * 客户端连接
     */
    private  Socket clientSocket;
    /**
     * 原始消息 反序列化后的对象
     */
    private  Object messageBean;

    /**
     * ReadWriteMode 读取的客户端原始消息
     */
    private  Object clientMessage;

    private  InetAddress clientAddress;
    private  int clientPort;
    public SocketRequest( Object clientMessage, InetAddress clientAddress, int clientPort) {
        this.clientMessage = clientMessage;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    public SocketRequest( Socket socket, Object clientMessage ) {
        this.clientSocket = socket;
        this.clientMessage = clientMessage;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public Object getMessageBean() {
        return messageBean;
    }

    public void setMessageBean(Object messageBean) {
        this.messageBean = messageBean;
    }

    public Object getClientMessage() {
        return clientMessage;
    }

    public void setClientMessage(Object clientMessage) {
        this.clientMessage = clientMessage;
    }
}
