package dev.simpleframework.token.config;

import dev.simpleframework.token.exception.InvalidTokenException;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * 登录配置
     */
    private SimpleTokenLoginConfig login = new SimpleTokenLoginConfig();
    /**
     * 不同账号类型的个性化登录配置，key 为 账号类型，未配置时取默认值 {@link #login}
     */
    private Map<String, SimpleTokenLoginConfig> accountLogin = new HashMap<>();
    /**
     * 路径配置
     */
    private SimpleTokenPathConfig path = new SimpleTokenPathConfig();

    /**
     * 获取账号类型对应的登录配置，未配置时取默认值 {@link #login}
     *
     * @param accountType 账号类型
     * @return 登录配置
     */
    public SimpleTokenLoginConfig findLoginConfig(String accountType) {
        SimpleTokenLoginConfig config = this.accountLogin.get(accountType);
        if (config == null) {
            config = this.login;
        }
        return config;
    }

    public List<SimpleTokenLoginConfig> listLoginConfigs() {
        List<SimpleTokenLoginConfig> configs = new ArrayList<>();
        if (this.login != null) {
            configs.add(this.login);
        }
        configs.addAll(this.accountLogin.values());
        return configs;
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
