package org.xuzhaorui.filter;



import org.xuzhaorui.messageserialization.SocketMessageSerializer;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketResponse {
    private final OutputStream outputStream;

    // 添加用于追踪是否已经写入数据的状态
    // 添加volatile保证该状态更新和线程之间的可见性
    private volatile boolean hasWritten = false;

    // 用于记录写入次数的计数器
    private final AtomicInteger writeCount = new AtomicInteger(0);

    private  String requestUrl;

    private Object code;

    private Object message;

    /**
     * 命中的反序列化器
     */
    private SocketMessageSerializer hitSerializer;





    public SocketResponse(OutputStream outputStream) {
        this.outputStream = outputStream;

    }

    public OutputStream getOutputStream() {
        return outputStream;
    }





    // 标记已写入状态，并增加写入次数
    public void markWritten() {
        this.hasWritten = true;
        writeCount.incrementAndGet();
    }

    // 获取写入次数
    public int getWriteCount() {
        return writeCount.get();
    }

    // 判断是否已经写入
    public boolean isHasWritten() {
        return hasWritten;
    }



    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

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

    public SocketMessageSerializer getHitSerializer() {
        return hitSerializer;
    }

    public void setHitSerializer(SocketMessageSerializer hitSerializer) {
        this.hitSerializer = hitSerializer;
    }
}
