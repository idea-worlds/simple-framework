package dev.simpleframework.token.session;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenKickout {
    private String loginId;
    private String app;
    private SessionPerson person;

    public SimpleTokenKickout() {
    }

    public SimpleTokenKickout(String loginId) {
        this.loginId = loginId;
    }

    public SimpleTokenKickout(String loginId, String app) {
        this.loginId = loginId;
        this.app = app;
    }

    public void exec() {
        this.setPerson();
        if (this.person == null) {
            return;
        }
        if (this.app == null) {
            List<String> tokens = this.person.findAllTokens();
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除用户所有会话
            SessionManager.removePerson(this.loginId);
        } else {
            List<String> tokens = this.person.findAllTokens(this.app);
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除应用会话中对应的 token
            this.person.removeTokens(tokens);
            SessionManager.storePerson(this.loginId, this.person);
        }
    }

    public void execByToken(List<String> tokens) {
        this.setPerson();
        // 删除 session
        SessionManager.removeSessionByToken(tokens);
        // 删除用户所有会话中对应的 token
        if (this.person != null) {
            this.person.removeTokens(tokens);
            SessionManager.storePerson(this.loginId, this.person);
        }
    }

    private void setPerson() {
        if (this.person != null) {
            return;
        }
        this.person = SessionManager.findPerson(this.loginId);
    }

}
