package org.xuzhaorui.filter;


import java.util.List;

/**
 * 过滤链实现
 */
public class SocketFilterChain {
    /**
     * 默认组
     */
    public static final String DEFAULT_FILTER_CHAIN = "defaultFilterChain";

    private final List<SocketFilter> filters;

    private int currentPosition = 0;

    public SocketFilterChain(List<SocketFilter> filters) {
        this.filters = filters;
    }


    public void doFilter(SocketRequest request, SocketResponse response) throws Exception {
        if (currentPosition < filters.size()) {
            // 依次调用过滤器
            SocketFilter currentFilter = filters.get(currentPosition++);
            currentFilter.doFilter(request, response, this);
        }
    }


}
