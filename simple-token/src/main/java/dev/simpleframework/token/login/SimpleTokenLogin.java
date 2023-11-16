package dev.simpleframework.token.login;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.constant.TokenStyle;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.exception.SimpleTokenException;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SessionPerson;
import dev.simpleframework.util.Strings;
import lombok.Getter;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
public class SimpleTokenLogin {

    private final String id;
    private final String app;
    private Duration timeout;
    private final SimpleTokenLoginConfig config;
    private final long now = System.currentTimeMillis();
    private SessionInfo session;
    /**
     * 当前用户的会话值
     */
    private SessionPerson person;
    /**
     * 当前用户过期的 token
     */
    private List<String> expiredTokens;

    public SimpleTokenLogin(String id, LoginSetting setting) {
        if (Strings.isBlank(id)) {
            throw new SimpleTokenException("Login id can not be null");
        }
        this.id = id;
        this.app = setting.getApp();
        this.timeout = setting.getTimeout();
        this.config = SimpleTokens.getGlobalConfig().getLogin();
        if (this.timeout == null) {
            this.timeout = this.config.getTokenTimeout();
        }
    }

    public void exec() {
        UserInfo user = UserManager.findInfoById(this.id);
        // 过期时间
        long expiredTime = this.timeout.toMillis() + this.now;
        // 是否需要存储 session
        boolean needStore = this.needStore();
        // 查找当前用户是否已登录，未登录时构建一个新的用户会话值
        this.person = needStore ? SessionManager.findPerson(this.id) : null;
        if (this.person == null) {
            this.person = new SessionPerson(this.id);
        }
        // 共享 token 时查出最后过期的会话
        String shareToken = null;
        if (needStore && this.config.getShareToken()) {
            shareToken = this.person.findLastExpiredToken();
            this.session = SessionManager.findSession(shareToken);
        }
        if (this.session == null) {
            if (shareToken != null) {
                // 有共享 token 又查无对应的会话值，说明是垃圾数据，应清除
                this.person.removeTokens(Collections.singleton(shareToken));
                shareToken = null;
            }
            this.session = SessionManager.createSession(this.id, user.getName(), expiredTime);
        }

        if (needStore) {
            // 修改会话过期时间
            this.session.setExpiredTime(expiredTime);
            // 非共享 token 时添加应用会话信息，并根据配置的策略获取过期的 token 用于登录后踢出
            if (shareToken == null) {
                String token = this.session.getToken();
                this.person.addApp(this.app, token, this.now, expiredTime);
                this.expiredTokens = this.person.removeExpiredByConfig(this.config, this.app, token);
            }
            // 存储会话
            SessionManager.storeSession(this.session);
            // 存储应用会话
            SessionManager.storePerson(this.id, this.person);
        }

        // 存储 token 至上下文
        ContextManager.storeToken(this.getToken(), expiredTime);
    }

    public String getToken() {
        if (this.session == null) {
            return null;
        }
        return this.session.getToken();
    }

    private boolean needStore() {
        // jwt 风格的 token 不需要存储 session
        return this.config.getTokenStyle() != TokenStyle.JWT;
    }

}
