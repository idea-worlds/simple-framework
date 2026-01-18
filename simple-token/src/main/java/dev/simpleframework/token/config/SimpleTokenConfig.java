package dev.simpleframework.token.config;

import dev.simpleframework.token.exception.InvalidTokenException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
@ConfigurationProperties(prefix = "simple.token")
public class SimpleTokenConfig {

    /**
     * 存储器名称，用于缓存键，完整的 key = #{sessionName}:session:#{token}
     */
    private String sessionName = "simple-token";
    /**
     * token 名称，用于 context key、cookie key、header key
     */
    private String tokenName = "x-acs-token";
    /**
     * token 前缀
     */
    private String tokenPrefix = "";
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

    /**
     * 根据配置的前缀拼接 token
     */
    public String splicingTokenForStore(String token) {
        if (token == null) {
            return "";
        }
        if (this.tokenPrefix == null || this.tokenPrefix.isBlank()) {
            return token;
        }
        return this.tokenPrefix + token;
    }

    /**
     * 删除 token 前缀
     */
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

}
