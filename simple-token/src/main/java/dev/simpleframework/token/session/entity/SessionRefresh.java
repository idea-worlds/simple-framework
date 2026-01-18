package dev.simpleframework.token.session.entity;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.exception.InvalidTokenException;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SessionPerson;

import java.time.Duration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SessionRefresh {
    private final SessionInfo session;

    public SessionRefresh(SessionInfo session) {
        this.session = session;
    }

    public void exec() {
        SessionPerson person = SessionManager.findPerson(this.session.getLoginId());
        if (person == null) {
            throw new InvalidTokenException("not login");
        }
        // 重置过期时间
        long now = System.currentTimeMillis();
        Duration timeout = SimpleTokens.getGlobalConfig().getLogin().getTokenTimeout();
        long expiredTime = timeout.toMillis() + now;
        person.updateTokenExpiredTime(this.session.getToken(), expiredTime);
        this.session.setCreateTime(now);
        this.session.setExpiredTime(expiredTime);
        // 存储
        SessionManager.storeSession(this.session);
        SessionManager.storePerson(person);
    }

}
