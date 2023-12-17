package dev.simpleframework.token.session;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.constant.TokenStyle;
import dev.simpleframework.token.user.UserInfo;
import dev.simpleframework.token.user.UserManager;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenSessionRefresh {

    private final String loginId;
    private final SessionInfo session;
    private long expiredTime = 0L;
    private final boolean jwt = SimpleTokens.getGlobalConfig().tokenStyleCheck(TokenStyle.JWT);

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
        if (person == null && !this.jwt) {
            return;
        }
        if (this.expiredTime <= 0 && person != null) {
            this.expiredTime = person.findLastExpiredTime();
        }
        if (this.expiredTime <= 0) {
            this.expiredTime = SimpleTokens.getGlobalConfig().tokenExpiredTime();
        }
        UserInfo user = UserManager.findInfoById(this.loginId);
        SessionInfo newSession = SessionManager.createSession(user, this.expiredTime);
        if (this.session != null) {
            this.updateSession(this.session, newSession);
        } else if (person != null) {
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
        if (this.jwt) {
            session.setToken(newSession.getToken());
        }
        session.setExpiredTime(newSession.getExpiredTime());
        session.setAttrs(newSession.getAttrs());
        SessionManager.storeSession(session);
    }

}
