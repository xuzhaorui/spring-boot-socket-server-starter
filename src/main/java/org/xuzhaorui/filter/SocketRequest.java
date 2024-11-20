package org.xuzhaorui.filter;


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


    public SocketRequest(Socket clientSocket, Object clientMessage) {
        this.clientSocket = clientSocket;
        this.clientMessage = clientMessage;
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
