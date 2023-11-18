package dev.simpleframework.token.session;

import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenSessionModify {

    private final SessionPerson person;

    public SimpleTokenSessionModify(String loginId) {
        this.person = SessionManager.findPerson(loginId);
    }

    public void modifyAttrs(Map<String, Object> attrs) {
        if (this.person == null) {
            return;
        }
        for (String token : this.person.findAllTokens()) {
            SessionInfo session = SessionManager.findSession(token);
            if (session == null) {
                this.person.removeToken(token);
                continue;
            }
            session.setAttrs(attrs);
            SessionManager.storeSession(session);
        }
    }

    public void modifyAttr(String key, Object value) {
        if (this.person == null) {
            return;
        }
        for (String token : this.person.findAllTokens()) {
            SessionInfo session = SessionManager.findSession(token);
            if (session == null) {
                this.person.removeToken(token);
                continue;
            }
            session.addAttr(key, value);
            SessionManager.storeSession(session);
        }
    }

}
