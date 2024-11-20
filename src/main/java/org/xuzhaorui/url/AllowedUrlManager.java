package org.xuzhaorui.url;

import java.util.Set;

public interface AllowedUrlManager {

    public void addAllowedUrl(String url) ;
    public void setAllowedUrls(Set<String> urls) ;



    public boolean isUrlAllowed(String url);
}
