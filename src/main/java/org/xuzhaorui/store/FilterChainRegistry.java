package org.xuzhaorui.store;

import org.xuzhaorui.annotation.FilterChain;
import org.xuzhaorui.filter.SocketFilter;
import org.xuzhaorui.filter.SocketFilterChain;
import org.xuzhaorui.url.PathMatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 过滤链注册表
 */
public class FilterChainRegistry {

    private final Map<String, List<SocketFilter>> filterChains = new ConcurrentHashMap<>();


    public void register(String url, SocketFilter filter) {
        filterChains.computeIfAbsent(url, k -> new CopyOnWriteArrayList<>()).add(filter);

        // 修改排序逻辑，使 order 小的在前
        filterChains.get(url).sort(Comparator.comparingInt(f -> {
            FilterChain filterChain = f.getClass().getAnnotation(FilterChain.class);
            return filterChain != null ? filterChain.order() : Integer.MAX_VALUE; // 默认最大值
        }));
    }


    public List<SocketFilter> getFilters(String url) {
        List<String> matchedPatterns = new ArrayList<>();
        for (String pattern : filterChains.keySet()) {
            if (PathMatcher.match(pattern, url)) {
                matchedPatterns.add(pattern);
            }
        }

        if (!matchedPatterns.isEmpty()) {
            matchedPatterns.sort(Comparator.comparingInt(String::length).reversed());
            String bestMatch = matchedPatterns.get(0);
            return filterChains.get(bestMatch);
        }
        return filterChains.getOrDefault(SocketFilterChain.DEFAULT_FILTER_CHAIN, new ArrayList<>());
    }
}

