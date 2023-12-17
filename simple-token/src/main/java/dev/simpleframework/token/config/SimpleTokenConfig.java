package dev.simpleframework.token.config;

import dev.simpleframework.token.constant.TokenStyle;
import dev.simpleframework.token.exception.InvalidTokenException;
import lombok.Data;

/**
 * 配置类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SimpleTokenConfig {
    private static long TOKEN_RENEW_LIMIT_TIME = 0;

    /**
     * token 名称，用于 session key、cookie name、header name
     */
    private String tokenName = "simple-token";
    /**
     * token 前缀
     */
    private String tokenPrefix = "";
    /**
     * token 是否自动续签
     */
    private Boolean tokenAutoRenew = Boolean.TRUE;
    /**
     * cookie 配置
     */
    private SimpleTokenCookieConfig cookie = new SimpleTokenCookieConfig();
    /**
     * 登录配置
     */
    private SimpleTokenLoginConfig login = new SimpleTokenLoginConfig();
    /**
     * 路径配置
     */
    private SimpleTokenPathConfig path = new SimpleTokenPathConfig();

    public String splicingToken(String token) {
        if (token == null) {
            return "";
        }
        if (this.tokenPrefix == null || this.tokenPrefix.isBlank()) {
            return token;
        }
        return this.tokenPrefix + " " + token;
    }

    public String parseToken(String token) {
        if (token == null) {
            return "";
        }
        if (this.tokenPrefix == null || this.tokenPrefix.isBlank()) {
            return token;
        }
        if (!token.startsWith(this.tokenPrefix + " ")) {
            throw new InvalidTokenException("don't have the prefix " + this.tokenPrefix);
        }
        return token.substring(this.tokenPrefix.length() + 1);
    }

    public boolean tokenStyleCheck(TokenStyle style) {
        return this.getLogin().getTokenStyle() == style;
    }

    public long tokenExpiredTime() {
        return System.currentTimeMillis()
                + this.login.getTokenTimeout().toMillis();
    }

    public long tokenRenewLimitTime() {
        if (TOKEN_RENEW_LIMIT_TIME == 0) {
            TOKEN_RENEW_LIMIT_TIME = this.login.getTokenTimeout().toMillis() / 4;
        }
        return TOKEN_RENEW_LIMIT_TIME;
    }

}
