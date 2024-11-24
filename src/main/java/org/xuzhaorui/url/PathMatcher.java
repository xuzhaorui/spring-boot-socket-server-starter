package org.xuzhaorui.url;

/**
 * 路径匹配
 */
public class PathMatcher {

    /**
     * 判断给定的路径规则是否匹配给定的URL
     * @param pattern 路径规则，支持 * 和 **
     * @param url 需要匹配的url
     * @return 是否匹配
     */
    public static boolean match(String pattern, String url) {
        // 替换 ** 为匹配任意多层路径
        String regex = pattern.replace("**", "(.+)?");

        // 替换 * 为匹配单层路径
        regex = regex.replace("*", "[^/]+");

        // 添加起始符和结束符，确保匹配整个URL
        regex = "^" + regex + "$";

        // 对整个URL进行匹配
        return url.matches(regex);
    }

    /**
     *  检查是否某个路径模式完全匹配
     * @param pattern 模式
     * @param url url
     * @return 是否完全匹配
     */
    public static boolean fullMatch(String pattern, String url) {
        return pattern.equals(url);
    }

    public static void main(String[] args) {
        System.out.println(PathMatcher.match("/test1/**", "/test1/getData/ById"));  // true
        System.out.println(PathMatcher.match("/test2/*", "/test2/123"));           // true
        System.out.println(PathMatcher.match("/test2/*", "/test2/123/456"));       // false
        System.out.println(PathMatcher.match("/test3/*/data", "/test3/any/data")); // true
        System.out.println(PathMatcher.match("/test3/**/data", "/test3/any/path/data")); // true
    }



}
