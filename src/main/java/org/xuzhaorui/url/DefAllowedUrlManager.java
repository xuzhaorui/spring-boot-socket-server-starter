package org.xuzhaorui.url;



import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 允许的 url 管理器
 */

public class DefAllowedUrlManager implements AllowedUrlManager {
    // 使用 CopyOnWriteArraySet 确保线程安全
    private final Set<String> allowUrls = new CopyOnWriteArraySet<>();

    public void addAllowedUrl(String url) {
        allowUrls.add(url);
    }

    public void setAllowedUrls(Set<String> urls) {
        allowUrls.addAll(urls);
    }

    public boolean isUrlAllowed(String url) {
        return allowUrls.contains(url);
    }

    public Set<String> getAllowUrls() {
        return allowUrls;
    }
}

