package org.xuzhaorui.filter;



import java.io.OutputStream;
import java.net.Socket;

public class SocketResponse {
    private final Socket clientSocket;
    private final OutputStream outputStream;
    private  String requestUrl;

    private Object code;

    private Object message;




    public Object getCode() {
        return code;
    }

    public void setCode(Object code) {
        this.code = code;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }




    public SocketResponse(Socket clientSocket, OutputStream outputStream) {
        this.clientSocket = clientSocket;
        this.outputStream = outputStream;

    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
}
