package dev.simpleframework.token.session.entity;

import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SessionPerson;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SessionKick {
    private String loginId;
    private String client;
    private SessionPerson person;

    public SessionKick() {
    }

    public SessionKick(String loginId) {
        this.loginId = loginId;
    }

    public SessionKick(String loginId, String client) {
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
            SessionManager.storePerson(this.person);
        }
    }

    public void execByToken(List<String> tokens) {
        this.setPerson();
        // 删除 session
        SessionManager.removeSessionByToken(tokens);
        // 删除用户所有会话中对应的 token
        if (this.person != null) {
            this.person.removeTokens(tokens);
            SessionManager.storePerson(this.person);
        }
    }

    private void setPerson() {
        if (this.person != null) {
            return;
        }
        this.person = SessionManager.findPerson(this.loginId);
    }

}
