package org.xuzhaorui.filter;


/**
 * SocketFilter接口，类似于Spring的 Filter
 */
public interface SocketFilter {
    /**
        过滤方法
     * @param request 请求
     * @param response 响应
     * @param chain 过滤链
     * @throws Exception 异常
     */
    void doFilter(SocketRequest request, SocketResponse response, SocketFilterChain chain) throws Exception;

}
