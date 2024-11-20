package org.xuzhaorui.store;


import org.xuzhaorui.messageserialization.SocketMessageSerializer;

import java.util.List;

/**
 * @author: xzr
 * SocketMessage 的结构
 *
 */

public class SocketMessageInfo {
    /**
     * 解析优先级
     */
    private final int priority;


    /**
        socketUrl所在Bean结构
     */
    private List<String> socketUrlPaths;


    /**
     * socketCode所在Bean结构
     */
    private List<String> socketCodePaths;

    /**
     * socketMsg所在Bean结构
     */
    private List<String> socketMsgPaths;


    /**
     *
     * @param priority
     * @param serializeType
     */
    private SocketMessageSerializer<?, ?> socketMessageSerializer;


    public SocketMessageInfo(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public List<String> getSocketUrlPaths() {
        return socketUrlPaths;
    }

    public void setSocketUrlPaths(List<String> socketUrlPaths) {
        this.socketUrlPaths = socketUrlPaths;
    }



    public List<String> getSocketCodePaths() {
        return socketCodePaths;
    }

    public void setSocketCodePaths(List<String> socketCodePaths) {
        this.socketCodePaths = socketCodePaths;
    }

    public List<String> getSocketMsgPaths() {
        return socketMsgPaths;
    }

    public void setSocketMsgPaths(List<String> socketMsgPaths) {
        this.socketMsgPaths = socketMsgPaths;
    }



    /**
     * @param <T> 序列化后的数据类型
     * @param <R> 反序列化后的对象类型
     */
    public <T, R> SocketMessageSerializer<T, R> getSocketMessageSerializer() {
        // 强制类型转换成合适的序列化器
        return (SocketMessageSerializer<T, R>) socketMessageSerializer;
    }

    public void setSocketMessageSerializer(SocketMessageSerializer<?, ?> socketMessageSerializer) {
        this.socketMessageSerializer = socketMessageSerializer;
    }
}
