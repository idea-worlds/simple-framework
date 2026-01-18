package dev.simpleframework.token.config;

import dev.simpleframework.token.constant.LoginMaxStrategy;
import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录配置
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SimpleTokenLoginConfig {


    /**
     * 同账号是否共享 token
     */
    private Boolean shareToken = Boolean.FALSE;
    /**
     * token 超时时间，默认 30 天
     */
    private Duration tokenTimeout = Duration.ofDays(30);
    /**
     * 最大登录数量，默认 -1
     * <0 : 不限数量
     * 0  : 不许登录
     * 1  : 不许多地登录
     * >1 : 允许多地登录的最大值
     */
    private Integer maxNum = -1;
    /**
     * 登录数超出最大值后的策略
     */
    private LoginMaxStrategy maxStrategy = LoginMaxStrategy.KICK_OUT_FIRST_CREATE;
    /**
     * 各客户端的配置
     */
    private Map<String, TokenClientConfig> clients = new HashMap<>();

    public TokenClientConfig findClientConfig(String client) {
        TokenClientConfig config = this.clients.get(client);
        if (config == null) {
            config = new TokenClientConfig();
        }
        return config;
    }

    @Data
    public static class TokenClientConfig {
        /**
         * 最大登录数量，默认 1
         * <0 : 不限数量
         * 0  : 不许登录
         * 1  : 不许多地登录
         * >1 : 允许多地登录的最大值
         */
        private Integer maxNum = 1;
        /**
         * 登录数超出最大值后的策略
         */
        private LoginMaxStrategy maxStrategy = LoginMaxStrategy.KICK_OUT_FIRST_CREATE;
    }

}
