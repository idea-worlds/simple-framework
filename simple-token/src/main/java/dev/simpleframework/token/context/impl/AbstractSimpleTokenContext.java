package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.SimpleTokenConfig;
import dev.simpleframework.token.SimpleTokenCookieConfig;
import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.context.ContextCookie;
import dev.simpleframework.token.context.SimpleTokenContext;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class AbstractSimpleTokenContext<REQ, RES, STORE> implements SimpleTokenContext {

    protected abstract REQ getRequest();

    protected abstract RES getResponse();

    protected abstract STORE getStore();

    protected abstract String getParam(REQ req, String key);

    protected abstract String getHeader(REQ req, String key);

    protected abstract String getCookie(REQ req, String key);

    protected abstract void addHeader(RES res, String key, String value);

    protected abstract Object get(STORE store, String key);

    protected abstract void set(STORE store, String key, Object value);

    protected abstract void remove(STORE store, String key);

    @Override
    public void setToken(String token, long expiredTime) {
        SimpleTokenConfig config = SimpleTokens.getGlobalConfig();
        token = config.splicingToken(token);
        String key = config.getTokenName();
        this.set(this.getStore(), key, token);

        if (config.getTokenInCookie()) {
            int maxAge = (int) (expiredTime - System.currentTimeMillis()) / 1000;
            SimpleTokenCookieConfig cookieConfig = config.getCookie();
            ContextCookie cookie = new ContextCookie(config.getTokenName(), token)
                    .setMaxAge(maxAge)
                    .setDomain(cookieConfig.getDomain())
                    .setPath(cookieConfig.getPath())
                    .setSecure(cookieConfig.getSecure())
                    .setHttpOnly(cookieConfig.getHttpOnly())
                    .setSameSite(cookieConfig.getSameSite());
            this.addHeader(this.getResponse(), ContextCookie.HEADER_NAME, cookie.toString());
        }
    }

    /**
     * 依次从 store / request param / request header / request cookie 获取 token 值，再根据配置裁剪掉前缀
     */
    @Override
    public String getToken() {
        SimpleTokenConfig config = SimpleTokens.getGlobalConfig();
        String key = config.getTokenName();
        // 从 store 获取
        String token = (String) this.get(this.getStore(), key);
        if (token == null) {
            // 从 request 获取
            REQ request = this.getRequest();
            token = this.getParam(request, key);
            if (token == null) {
                token = this.getHeader(request, key);
            }
            if (token == null && config.getTokenInCookie()) {
                token = this.getCookie(request, key);
            }
        }
        token = config.parseToken(token);
        return token.isBlank() ? null : token;
    }

    /**
     * 依次删除 store / cookie 中的 token
     */
    @Override
    public String removeToken() {
        SimpleTokenConfig config = SimpleTokens.getGlobalConfig();
        String token = this.getToken();
        if (token == null) {
            return null;
        }
        String key = config.getTokenName();
        this.remove(this.getStore(), key);

        if (config.getTokenInCookie()) {
            SimpleTokenCookieConfig cookieConfig = config.getCookie();
            ContextCookie cookie = new ContextCookie(config.getTokenName(), null)
                    .setMaxAge(0)
                    .setDomain(cookieConfig.getDomain())
                    .setPath(cookieConfig.getPath())
                    .setSecure(cookieConfig.getSecure())
                    .setHttpOnly(cookieConfig.getHttpOnly())
                    .setSameSite(cookieConfig.getSameSite());
            this.addHeader(this.getResponse(), ContextCookie.HEADER_NAME, cookie.toString());
        }
        return token;
    }

    @Override
    public boolean matchPath(String pattern, String path) {
        return false;
    }

    @Override
    public boolean enable() {
        return false;
    }
}
