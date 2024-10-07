package dev.simpleframework.token.session;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.user.TokenUserInfo;
import dev.simpleframework.token.user.TokenUserManager;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenSessionRefresh {

    private final String loginId;
    private final SessionInfo session;
    private long expiredTime = 0L;

    public SimpleTokenSessionRefresh(String loginId) {
        this.loginId = loginId;
        this.session = null;
    }

    public SimpleTokenSessionRefresh(String loginId, long expiredTime) {
        this.loginId = loginId;
        this.session = null;
        this.expiredTime = expiredTime;
    }

    public SimpleTokenSessionRefresh(SessionInfo session) {
        this.loginId = null;
        this.session = session;
    }

    public SimpleTokenSessionRefresh(SessionInfo session, long expiredTime) {
        this.loginId = session.getLoginId();
        this.session = session;
        this.expiredTime = expiredTime;
    }

    public void exec() {
        SessionPerson person = SessionManager.findPerson(this.loginId);
        if (person == null) {
            return;
        }
        if (this.expiredTime <= 0 && person != null) {
            this.expiredTime = person.findLastExpiredTime();
        }
        if (this.expiredTime <= 0) {
            this.expiredTime = SimpleTokens.getGlobalConfig().tokenExpiredTime();
        }
        TokenUserInfo user = TokenUserManager.findInfoById(this.loginId);
        SessionInfo newSession = SessionManager.createSession(user, this.expiredTime);
        if (this.session != null) {
            this.updateSession(this.session, newSession);
        } else {
            for (String token : person.findAllTokens()) {
                SessionInfo session = SessionManager.findSession(token);
                if (session == null) {
                    person.removeToken(token);
                    continue;
                }
                this.updateSession(session, newSession);
            }
        }
        SessionManager.storePerson(this.loginId, person);
    }

    private void updateSession(SessionInfo session, SessionInfo newSession) {
        session.setExpiredTime(newSession.getExpiredTime());
        session.changeAttrs(newSession);
        SessionManager.storeSession(session);
    }

}
