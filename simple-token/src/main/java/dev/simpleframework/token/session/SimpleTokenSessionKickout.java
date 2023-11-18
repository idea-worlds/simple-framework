package dev.simpleframework.token.session;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenSessionKickout {
    private String loginId;
    private String client;
    private SessionPerson person;

    public SimpleTokenSessionKickout() {
    }

    public SimpleTokenSessionKickout(String loginId) {
        this.loginId = loginId;
    }

    public SimpleTokenSessionKickout(String loginId, String client) {
        this.loginId = loginId;
        this.client = client;
    }

    public void exec() {
        this.setPerson();
        if (this.person == null) {
            return;
        }
        if (this.client == null) {
            List<String> tokens = this.person.findAllTokens();
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除用户所有会话
            SessionManager.removePerson(this.loginId);
        } else {
            List<String> tokens = this.person.findAllTokens(this.client);
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除用户所有会话中对应的 token
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
