package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionPerson;
import dev.simpleframework.token.session.SessionStore;

import java.time.Duration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class AbstractSessionStore implements SessionStore {

    protected abstract void setInfoData(String key, SessionInfo value, Duration timeout);

    protected abstract void setPersonData(String key, SessionPerson value, Duration timeout);

    protected abstract SessionInfo getInfoData(String key);

    protected abstract SessionPerson getPersonData(String key);

    protected abstract void remove(String key);

    @Override
    public void setSession(SessionInfo session) {
        String key = this.toSessionKey(session.getToken());
        Duration timeout = Duration.ofMillis(session.getExpiredTime() - System.currentTimeMillis());
        this.setInfoData(key, session, timeout);
    }

    @Override
    public SessionInfo getSession(String token) {
        String key = this.toSessionKey(token);
        return this.getInfoData(key);
    }

    @Override
    public void removeSession(String token) {
        String key = this.toSessionKey(token);
        this.remove(key);
    }

    @Override
    public void setPerson(SessionPerson person) {
        String key = this.toPersonKey(person.getLoginId());
        long expiredTime = person.findLastExpiredTime();
        long timeout = expiredTime - System.currentTimeMillis();
        if (timeout <= 0) {
            this.remove(key);
        } else {
            this.setPersonData(key, person, Duration.ofMillis(timeout));
        }
    }

    @Override
    public SessionPerson getPerson(String loginId) {
        String key = this.toPersonKey(loginId);
        return this.getPersonData(key);
    }

    @Override
    public void removePerson(String loginId) {
        String key = this.toPersonKey(loginId);
        this.remove(key);
    }

    protected String toSessionKey(String token) {
        SimpleTokenConfig config = SimpleTokens.getGlobalConfig();
        return config.getSessionName() + ":session:" + token;
    }

    protected String toPersonKey(String loginId) {
        SimpleTokenConfig config = SimpleTokens.getGlobalConfig();
        return config.getSessionName() + ":person:" + loginId;
    }

}
