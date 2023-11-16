package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.constant.TokenStyle;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionStore;
import dev.simpleframework.token.session.SessionPerson;

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
        SessionInfo session = this.get(key, SessionInfo.class);
        if (session == null) {
            // 未查到会话值时，尝试解析 jwt
            SimpleTokenLoginConfig config = SimpleTokens.getGlobalConfig().getLogin();
            if (config.getTokenStyle() == TokenStyle.JWT) {
                try {
                    String secretKey = config.getTokenJwtSecretKey();
                    session = DefaultJwtToken.of(secretKey, token).getSession();
                } catch (Exception ignore) {
                    return null;
                }
            }
        }
        return session;
    }

    @Override
    public void removeSession(String token) {
        String key = this.toSessionKey(token);
        this.remove(key);
    }

    @Override
    public void setPerson(String loginId, SessionPerson person) {
        String key = this.toPersonKey(loginId);
        long expiredTime = person.findLastExpiredTime();
        if (expiredTime == 0) {
            this.remove(key);
        } else {
            Duration timeout = Duration.ofMillis(person.findLastExpiredTime() - System.currentTimeMillis());
            this.set(key, person, timeout);
        }
    }

    @Override
    public SessionPerson getPerson(String loginId) {
        String key = this.toPersonKey(loginId);
        return this.get(key, SessionPerson.class);
    }

    @Override
    public void removePerson(String loginId) {
        String key = this.toPersonKey(loginId);
        this.remove(key);
    }

    protected String toSessionKey(String token) {
        String prefix = SimpleTokens.getGlobalConfig().getTokenName();
        return prefix + ":session:" + token;
    }

    protected String toPersonKey(String loginId) {
        String prefix = SimpleTokens.getGlobalConfig().getTokenName();
        return prefix + ":person:" + ":" + loginId;
    }

}
