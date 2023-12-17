package dev.simpleframework.token.context;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenConfig;

/**
 * 上下文处理器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface SimpleTokenContext {

    /**
     * 获取当前上下文请求对象
     */
    ContextRequest request();

    /**
     * 获取当前上下文返回值对象
     */
    ContextResponse response();

    /**
     * 获取当前上下文存储器对象
     */
    ContextStore store();

    /**
     * 存储 token
     *
     * @param token       token
     * @param expiredTime 过期时间
     */
    default void setToken(String token, long expiredTime) {
        SimpleTokenConfig config = SimpleTokens.getGlobalConfig();
        token = config.splicingToken(token);
        String tokenName = config.getTokenName();
        this.store().set(tokenName, token);

        ContextResponse response = this.response();
        response.addHeader(tokenName, token);

        int cookieAge = (int) (expiredTime - System.currentTimeMillis()) / 1000;
        response.addCookie(tokenName, token, cookieAge);
    }

    /**
     * 获取 token
     * * 依次从 store / request param / request header / request cookie 获取 token 值，再根据配置裁剪掉前缀
     *
     * @return token
     */
    default String getToken() {
        SimpleTokenConfig config = SimpleTokens.getGlobalConfig();
        String key = config.getTokenName();
        // 从 store 获取
        String token = this.store().get(key);
        if (token == null) {
            // 从 request 获取
            ContextRequest request = this.request();
            token = request.getParam(key);
            if (token == null) {
                token = request.getHeader(key);
            }
            if (token == null) {
                token = request.getCookie(key);
            }
        }
        token = config.parseToken(token);
        return token.isBlank() ? null : token;
    }

    /**
     * 删除 token
     * * 依次删除 store / cookie 中的 token
     *
     * @return token
     */
    default String removeToken() {
        SimpleTokenConfig config = SimpleTokens.getGlobalConfig();
        String token = this.getToken();
        if (token == null) {
            return null;
        }
        String key = config.getTokenName();
        this.store().remove(key);
        this.response().removeCookie(config.getTokenName());
        return token;
    }

    /**
     * 在本次请求中此上下文是否可用
     */
    boolean enable();

}
