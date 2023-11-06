package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionStore;
import dev.simpleframework.token.session.SimpleTokenApps;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class AbstractSessionStore implements SessionStore {

    protected abstract void set(String key, Object value, long timeout);

    protected abstract <T> T get(String key);

    protected abstract void remove(String key);

    @Override
    public void setSession(SessionInfo session) {
        String key = this.toSessionKey(session.getToken());
        long timeout = session.getExpiredTime() - System.currentTimeMillis();
        this.set(key, session, timeout);
    }

    @Override
    public void setApps(String accountType, String loginId, SimpleTokenApps apps) {
        String key = this.toAppsKey(accountType, loginId);
        long expiredTime = apps.findLastExpiredTime();
        if (expiredTime == 0) {
            this.remove(key);
        } else {
            long timeout = apps.findLastExpiredTime() - System.currentTimeMillis();
            this.set(key, apps, timeout);
        }
    }

    @Override
    public SessionInfo getSession(String token) {
        String key = this.toSessionKey(token);
        return this.get(key);
    }

    @Override
    public SimpleTokenApps getApps(String accountType, String loginId) {
        String key = this.toAppsKey(accountType, loginId);
        return this.get(key);
    }

    @Override
    public void removeSession(String token) {
        String key = this.toSessionKey(token);
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
