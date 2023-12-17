package dev.simpleframework.token.context;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenCookieConfig;

/**
 * 上下文返回值对象
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface ContextResponse {

    /**
     * 返回值添加头部信息
     *
     * @param name  名
     * @param value 值
     */
    void addHeader(String name, String value);

    /**
     * 添加 cookie
     *
     * @param name    名
     * @param value   值
     * @param timeout 超时时间（秒）
     */
    default void addCookie(String name, String value, int timeout) {
        SimpleTokenCookieConfig cookieConfig = SimpleTokens.getGlobalConfig().getCookie();
        ContextCookie cookie = new ContextCookie(name, value)
                .setMaxAge(timeout)
                .setDomain(cookieConfig.getDomain())
                .setPath(cookieConfig.getPath())
                .setSecure(cookieConfig.getSecure())
                .setHttpOnly(cookieConfig.getHttpOnly())
                .setSameSite(cookieConfig.getSameSite());
        this.addHeader(ContextCookie.HEADER_NAME, cookie.toString());
    }

    /**
     * 删除 cookie
     *
     * @param name 名
     */
    default void removeCookie(String name) {
        this.addCookie(name, null, 0);
    }

}
