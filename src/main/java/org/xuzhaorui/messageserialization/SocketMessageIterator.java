package org.xuzhaorui.messageserialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.exception.pre.MessageSerializationException;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;
import org.xuzhaorui.store.SocketMessageInfo;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *Socket消息迭代器
 */
public class SocketMessageIterator {

    private static final Logger log = LoggerFactory.getLogger(SocketMessageIterator.class);


    /**
     * 反序列化迭代
     * @param socketMessages 消息存储表
     * @param request 请求
     * @param response 响应
     */
    public static void iterateAndDeserialize(ConcurrentHashMap<Class<?>, SocketMessageInfo> socketMessages,  SocketRequest request, SocketResponse response) {
        Object clientMessage = request.getClientMessage();
        for (Map.Entry<Class<?>, SocketMessageInfo> entry : socketMessages.entrySet()) {
            Class<?> key = entry.getKey();
            SocketMessageInfo value = entry.getValue();
            Object result;
            SocketMessageSerializer<Object, Object> socketMessageSerializer =  value.getSocketMessageSerializer();
            try {
                result = socketMessageSerializer.deserialize(clientMessage, (Class<Object>) key);
            } catch (Exception e) {
                // 如果反序列化失败，记录日志并继续处理下一个适配器
                log.warn("反序列化失败: {}, 类: {}, 调用下一个适配器", e.getMessage(), key.getName());
                continue;
            }
            if (Objects.nonNull(result)) {
                request.setMessageBean(result);
                break;
            }else {
                // 如果没有找到合适的序列化器，则抛出异常
                throw new MessageSerializationException("反序列化失败，没有适配器可以处理");
            }
        }

    }

    /**
     * 序列化迭代
     * @param socketMessages 标记@SocketMessage集合
     * @param data           JavaBean
     * @return 序列化后的数据
     */
    public static Object iterateAndSerialize(ConcurrentHashMap<Class<?>, SocketMessageInfo> socketMessages, Object data) {
        for (Map.Entry<Class<?>, SocketMessageInfo> entry : socketMessages.entrySet()) {
            SocketMessageInfo value = entry.getValue();
            Object result;
            try {
                SocketMessageSerializer<?,Object> serializer = value.getSocketMessageSerializer();
                result = serializer.serialize(data);
            } catch (Exception e) {
                // 如果序列化失败，记录日志并继续处理下一个适配器
                log.warn("序列化失败: {}, 调用下一个适配器", e.getMessage());
                continue;
            }
            if (Objects.nonNull(result)) return result;
        }
        // 如果没有找到合适的序列化器，则抛出异常
        throw new MessageSerializationException("序列化失败，没有适配器可以处理");
    }
}
