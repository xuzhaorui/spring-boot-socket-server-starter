package org.xuzhaorui.messageserialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.xuzhaorui.exception.pre.MessageSerializationException;
import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;
import org.xuzhaorui.store.SocketMessageInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    public static void iterateAndDeserialize(ConcurrentHashMap<Class<?>, SocketMessageInfo> socketMessages, SocketRequest request, SocketResponse response) {
        // 使用 AtomicBoolean 来标记是否成功反序列化
        AtomicBoolean deserializationSuccessful = new AtomicBoolean(false);

        for (Map.Entry<Class<?>, SocketMessageInfo> entry : socketMessages.entrySet()) {
            Class<?> key = entry.getKey();
            SocketMessageInfo socketMessageInfo = entry.getValue();

            try {
                Object clientMessage = request.getClientMessage();
                Object result = socketMessageInfo.deserialize(clientMessage, key,response);
                // 反序列化成功，设置消息并退出循环
                request.setMessageBean(result);
                deserializationSuccessful.set(true);  // 更新原子布尔变量状态
                break;

            } catch (Exception e) {
                // 反序列化失败，记录日志并继续尝试下一个序列化器
//                log.warn("反序列化失败: {}, 类: {}, 调用下一个适配器", e.getMessage(), key.getName());
            }
        }

        // 如果没有任何适配器成功反序列化，抛出异常
        if (!deserializationSuccessful.get()) {
            throw new MessageSerializationException("反序列化失败，没有适配器可以处理");
        }
    }
    /**
     * 序列化迭代
     * @param socketMessages 标记@SocketMessage集合
     * @param data           JavaBean
     * @return 序列化后的数据
     */
    public static Object iterateAndSerialize(ConcurrentHashMap<Class<?>, SocketMessageInfo> socketMessages, Object data) {
        // 使用 AtomicReference 来存储序列化结果
        AtomicReference<Object> serializedResult = new AtomicReference<>(null);

        for (Map.Entry<Class<?>, SocketMessageInfo> entry : socketMessages.entrySet()) {
            SocketMessageInfo socketMessageInfo = entry.getValue();

            try {
                Object result = socketMessageInfo.serialize(data);
                if (Objects.nonNull(result)) {
                    serializedResult.set(result);  // 如果序列化成功，设置结果
                    break;  // 退出循环
                }
            } catch (Exception e) {
                // 如果序列化失败，记录日志并继续处理下一个适配器
//                log.warn("序列化失败: {}, 调用下一个适配器", e.getMessage());
            }
        }

        // 如果没有成功序列化，抛出异常
        if (serializedResult.get() == null) {
            throw new MessageSerializationException("序列化失败，没有适配器可以处理");
        }

        return serializedResult.get();  // 返回序列化结果
    }
}
