package dev.simpleframework.token.login;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Setter
@Getter
public class LoginSetting {

    /**
     * 当前登录的应用
     */
    private String app;
    /**
     * 超时时间，未设值时默认取 SimpleTokenLoginConfig.timeout 配置的值
     */
    private Duration timeout;

    public LoginSetting() {
        this("default");
    }

    public LoginSetting(String app) {
        this.app = app;
    }

}
