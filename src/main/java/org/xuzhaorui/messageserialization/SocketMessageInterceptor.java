package org.xuzhaorui.messageserialization;



import org.xuzhaorui.filter.SocketRequest;
import org.xuzhaorui.filter.SocketResponse;
import org.xuzhaorui.store.SocketMessageInfoRegistry;

import javax.annotation.Resource;

/**
 * 消息拦截器
 */
public class SocketMessageInterceptor {


    @Resource
    private SocketMessageInfoRegistry socketMessageInfoRegistry;

    /**
     * 反序列化
     * @param request 请求
     * @param response 响应
     * @throws RuntimeException 异常
     */
    public void deserialize(SocketRequest request, SocketResponse response) throws RuntimeException {
         SocketMessageIterator.iterateAndDeserialize(socketMessageInfoRegistry.getSocketMessages(), request,response);
    }

    /**
     * 处理序列化
     * @param data JavaBean
     * @throws RuntimeException 异常
     * @return 序列化数据
     */
    public Object serialize(Object data) throws RuntimeException {
        return SocketMessageIterator.iterateAndSerialize(socketMessageInfoRegistry.getSocketMessages(), data);
    }
}

