package dev.simpleframework.token.context;

import java.util.List;

/**
 * 上下文处理器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface SimpleTokenContext {

    /**
     * 存储 token
     *
     * @param token       token
     * @param expiredTime 过期时间
     */
    void setToken(String token, long expiredTime);

    /**
     * 获取 token
     *
     * @return token
     */
    String getToken();

    /**
     * 删除 token
     *
     * @return token
     */
    String removeToken();

    /**
     * 获取请求的路径
     *
     * @return 路径
     */
    String getRequestPath();

    /**
     * 指定路由匹配符是否匹配指定路径
     *
     * @param pattern 路由匹配符
     * @param path    需要匹配的路径
     * @return 是否匹配
     */
    boolean matchPath(String pattern, String path);

    /**
     * 指定路由匹配符是否匹配指定路径
     *
     * @param patterns 路由匹配符
     * @param path     需要匹配的路径
     * @return 是否匹配
     */
    default boolean matchPath(List<String> patterns, String path) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (this.matchPath(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在本次请求中此上下文是否可用
     */
    boolean enable();

}
