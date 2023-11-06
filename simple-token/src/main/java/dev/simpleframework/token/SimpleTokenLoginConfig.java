package dev.simpleframework.token;

import dev.simpleframework.token.constant.LoginMaxStrategy;
import dev.simpleframework.token.constant.TokenStyle;
import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SimpleTokenLoginConfig {

    private Boolean shareToken = Boolean.FALSE;
    /**
     * token 风格
     */
    private TokenStyle tokenStyle = TokenStyle.UUID32;
    /**
     * token jwt 的密钥
     */
    private String tokenJwtSecretKey;
    /**
     * token 超时时间，默认 1 天
     */
    private Duration timeout = Duration.ofDays(1);
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
     * 各应用的配置
     */
    private Map<String, AppConfig> apps = new HashMap<>();

    public AppConfig findAppConfig(String app) {
        AppConfig config = this.apps.get(app);
        if (config == null) {
            config = new AppConfig();
        }
        return config;
    }

    @Data
    public static class AppConfig {
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
