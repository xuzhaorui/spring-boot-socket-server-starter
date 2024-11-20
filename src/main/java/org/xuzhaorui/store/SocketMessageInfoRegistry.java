package org.xuzhaorui.store;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * socket传输消息序列化注册表
 */

public class SocketMessageInfoRegistry {
    // 存储扫描到的Bean类及其注解信息
    private ConcurrentHashMap<Class<?>, SocketMessageInfo> socketMessages = new ConcurrentHashMap<>();


    public ConcurrentHashMap<Class<?>, SocketMessageInfo> getSocketMessages() {
        return socketMessages;
    }

    public void setSocketMessages(ConcurrentHashMap<Class<?>, SocketMessageInfo> socketMessages) {
        this.socketMessages = socketMessages;


    }



    public void addSocketMessage(Class<?> clazz, SocketMessageInfo info) {
        socketMessages.put(clazz, info);

    }

    public SocketMessageInfo getSocketMessageInfo(Class<?> clazz) {
        return socketMessages.get(clazz);
    }

    public Collection<SocketMessageInfo> getAllSocketMessages() {
        return socketMessages.values();
    }


}
