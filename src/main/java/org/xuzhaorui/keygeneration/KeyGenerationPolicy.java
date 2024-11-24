package org.xuzhaorui.keygeneration;

import org.xuzhaorui.filter.SocketRequest;

/**
 * 生成缓存key策略
 */
public interface KeyGenerationPolicy {

    /**
     * 生成缓存key
     * @return 缓存key
     */
   String generationKey(SocketRequest request) throws Exception;
}
