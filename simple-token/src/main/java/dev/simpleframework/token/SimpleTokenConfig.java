package dev.simpleframework.token;

import dev.simpleframework.token.exception.InvalidTokenException;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SimpleTokenConfig {

    /**
     * token 名称，用于 session 前缀、cookie 名称
     */
    private String tokenName = "stoken";
    /**
     * token 前缀
     */
    private String tokenPrefix = "";
    /**
     * cookie 里是否有 token
     */
    private Boolean tokenInCookie = Boolean.TRUE;
    /**
     * cookie 配置
     */
    private SimpleTokenCookieConfig cookie = new SimpleTokenCookieConfig();
    /**
     * 登录配置，key 为 账号类型
     */
    private Map<String, SimpleTokenLoginConfig> login = new HashMap<>();

    public SimpleTokenLoginConfig findLoginConfig(String type) {
        return this.login.get(type);
    }

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
            return null;
        }
        if (this.tokenPrefix == null || this.tokenPrefix.isBlank()) {
            return token;
        }
        if (!token.startsWith(this.tokenPrefix + " ")) {
            throw new InvalidTokenException("don't have the prefix " + this.tokenPrefix);
        }
        return token.substring(this.tokenPrefix.length() + 1);
    }

}