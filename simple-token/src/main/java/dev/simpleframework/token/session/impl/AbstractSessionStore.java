package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionStore;
import dev.simpleframework.token.session.SimpleTokenApps;

import java.time.Duration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class AbstractSessionStore implements SessionStore {

    protected abstract void set(String key, Object value, Duration timeoutSecond);

    protected abstract <T> T get(String key, Class<T> clazz);

    protected abstract void remove(String key);

    @Override
    public void setSession(SessionInfo session) {
        String key = this.toSessionKey(session.getToken());
        Duration timeout = Duration.ofMillis(session.getExpiredTime() - System.currentTimeMillis());
        this.set(key, session, timeout);
    }

    @Override
    public SessionInfo getSession(String token) {
        String key = this.toSessionKey(token);
        return this.get(key, SessionInfo.class);
    }

    @Override
    public void removeSession(String token) {
        String key = this.toSessionKey(token);
        this.remove(key);
    }

    @Override
    public void setApps(String accountType, String loginId, SimpleTokenApps apps) {
        String key = this.toAppsKey(accountType, loginId);
        long expiredTime = apps.findLastExpiredTime();
        if (expiredTime == 0) {
            this.remove(key);
        } else {
            Duration timeout = Duration.ofMillis(apps.findLastExpiredTime() - System.currentTimeMillis());
            this.set(key, apps, timeout);
        }
    }

    @Override
    public SimpleTokenApps getApps(String accountType, String loginId) {
        String key = this.toAppsKey(accountType, loginId);
        return this.get(key, SimpleTokenApps.class);
    }

    @Override
    public void removeApps(String accountType, String loginId) {
        String key = this.toAppsKey(accountType, loginId);
        this.remove(key);
    }

    protected String toSessionKey(String token) {
        String prefix = SimpleTokens.getGlobalConfig().getTokenName();
        return prefix + ":session:" + token;
    }

    protected String toAppsKey(String accountType, String loginId) {
        String prefix = SimpleTokens.getGlobalConfig().getTokenName();
        return prefix + ":apps:" + accountType + ":" + loginId;
    }

}
