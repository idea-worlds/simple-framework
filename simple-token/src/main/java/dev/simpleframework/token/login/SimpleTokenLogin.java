package dev.simpleframework.token.login;

import dev.simpleframework.token.SimpleTokenLoginConfig;
import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.exception.SimpleTokenException;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SimpleTokenApps;
import dev.simpleframework.token.session.SimpleTokenSession;
import dev.simpleframework.util.Strings;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
public class SimpleTokenLogin {

    private final String id;
    private final LoginSetting setting;
    private final SimpleTokenLoginConfig config;
    private final long now = System.currentTimeMillis();
    private SimpleTokenSession session;
    /**
     * 当前账号的应用会话值
     */
    private SimpleTokenApps apps;
    /**
     * 当前账号过期的 token
     */
    private List<String> expiredTokens;

    public SimpleTokenLogin(String id, LoginSetting setting) {
        if (Strings.isBlank(id)) {
            throw new SimpleTokenException("Login id can not be null");
        }
        this.id = id;
        this.setting = setting;
        this.config = SimpleTokens.getGlobalConfig().findLoginConfig(setting.getAccountType());
        if (this.setting.getTimeout() == null) {
            this.setting.setTimeout(this.config.getTimeout());
        }
    }

    public void exec() {
        // 过期时间
        long expiredTime = this.setting.getTimeout().toMillis() + this.now;
        // 查找当前账号是否已登录，未登录时构建一个新的应用会话值
        this.apps = SessionManager.findApps(this.getType(), this.id);
        if (this.apps == null) {
            this.apps = new SimpleTokenApps(this.getType(), this.id);
        }
        // 共享 token 时查出最后过期的会话
        String shareToken = null;
        if (this.config.getShareToken()) {
            shareToken = this.apps.findLastExpiredToken();
            this.session = SessionManager.findSession(shareToken);
        }
        if (this.session == null) {
            if (shareToken != null) {
                // 有共享 token 又查无对应的会话值，说明是垃圾数据，应清除
                this.apps.removeTokens(Collections.singleton(shareToken));
                shareToken = null;
            }
            this.session = SessionManager.createSession(this.getType(), this.id, expiredTime);
        }
        // 修改会话过期时间
        this.session.setExpiredTime(expiredTime);
        // 非共享 token 时添加应用会话信息，并根据配置的策略获取过期的 token 用于登录后踢出
        if (shareToken == null) {
            String app = this.setting.getApp();
            String token = this.session.getToken();
            this.apps.addApp(app, token, this.now, expiredTime);
            this.expiredTokens = this.apps.removeExpiredByConfig(this.config, app, token);
        }

        // 存储会话
        SessionManager.storeSession(this.session);
        // 存储应用会话
        SessionManager.storeApps(this.getType(), this.id, this.apps);
        // 存储 token 至上下文
        ContextManager.storeToken(this.getToken(), expiredTime);
    }

    public String getToken() {
        if (this.session == null) {
            return null;
        }
        return this.session.getToken();
    }

    public String getType() {
        return this.setting.getAccountType();
    }

}
