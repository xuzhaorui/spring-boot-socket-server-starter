package org.xuzhaorui.store;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.filter.SocketResponse;
import org.xuzhaorui.messageserialization.SocketMessageSerializer;

import java.util.List;

/**
 * @author: xzr
 * SocketMessage 的结构
 *
 */

public class SocketMessageInfo  {
    private static final Logger log = LoggerFactory.getLogger(SocketMessageInfo.class);
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
     *ClientAsServer所在Bean结构
     */
    private List<String> clientAsServerPaths;

    /**
     *
     * @param priority
     * @param serializeType
     */
    private List<SocketMessageSerializer> socketMessageSerializerList;


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

    public List<SocketMessageSerializer> getSocketMessageSerializerList() {
        return socketMessageSerializerList;
    }

    public void setSocketMessageSerializerList(List<SocketMessageSerializer> socketMessageSerializerList) {
        this.socketMessageSerializerList = socketMessageSerializerList;
    }


    public List<String> getClientAsServerPaths() {
        return clientAsServerPaths;
    }

    public void setClientAsServerPaths(List<String> clientAsServerPaths) {
        this.clientAsServerPaths = clientAsServerPaths;
    }

    public Object deserialize(Object message, Class<?> clazz, SocketResponse response) throws Exception {
        for (SocketMessageSerializer socketMessageSerializer : socketMessageSerializerList) {
            try {
                Object result = socketMessageSerializer.deserialize(message, clazz);
                if (result != null) {
                    response.setHitSerializer(socketMessageSerializer);
                    return result; // 提前返回，减少不必要的判断
                }
            } catch (Exception e) {
                // 捕获反序列化失败时的异常，记录日志并继续尝试下一个序列化器
                log.warn("使用 {} 序列化器，反序列化 {} 失败，错误: {}，尝试下一个序列化器",
                        socketMessageSerializer.getClass().getName(), clazz.getName(), e.getMessage());
            }
        }

        // 如果所有序列化器都失败，记录错误日志并抛出异常
        log.error("对象 {} 的所有序列化器均反序列化失败，序列化器详见 SocketMessage 中 serializer", clazz.getName());
        throw new Exception("反序列化失败: 无法找到适合的序列化器处理 " + clazz.getName());
    }


    public Object serialize(Object data) throws Exception {
        for (SocketMessageSerializer socketMessageSerializer : socketMessageSerializerList) {
            try {
                Object serialize =   socketMessageSerializer.serialize(data);
                if (serialize != null) {
                    return serialize;
                }
            } catch (Exception e) {
                log.warn("使用{}序列化器，序列化{}失败, 调用下一个序列化器",socketMessageSerializer.getClass().getName(),data.getClass().getName());
            }
        }
        log.error("对象{}的所有序列化器，序列化失败，序列化器详见SocketMessage中serializer",data.getClass().getName());
        throw new Exception("序列化失败: 无法找到适合的序列化器处理 " +data.getClass().getName());


    }
}
