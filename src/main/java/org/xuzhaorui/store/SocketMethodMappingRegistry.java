package org.xuzhaorui.store;

import org.xuzhaorui.scanner.SocketHandlerMethod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Socket请求url映射表
 */
public class SocketMethodMappingRegistry {

    // 使用线程安全的 ConcurrentHashMap 替代普通的 HashMap
    private final Map<String, SocketHandlerMethod> handlerMappings = new ConcurrentHashMap<>();

    // 判断url是否存在
    public boolean containsMapping(String url) {
        return handlerMappings.containsKey(url);
    }

    // 注册Socket消息映射
    public void registerMapping(String url, SocketHandlerMethod handlerMethod) {
        handlerMappings.put(url, handlerMethod);  // 这里的 put 操作已经是线程安全的
    }

    // 获取Socket消息映射
    public SocketHandlerMethod getHandler(String url) {
        return handlerMappings.get(url);  // 直接读取也是线程安全的
    }
}
