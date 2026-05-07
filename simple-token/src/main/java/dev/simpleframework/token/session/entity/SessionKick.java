package dev.simpleframework.token.session.entity;

import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SessionPerson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 先查出各 token 对应的 loginId（删除前查，否则 session 已不存在）
        Map<String, List<String>> tokensByLogin = new HashMap<>();
        for (String token : tokens) {
            SessionInfo session = SessionManager.findSession(token);
            if (session != null) {
                tokensByLogin.computeIfAbsent(session.getLoginId(), k -> new ArrayList<>()).add(token);
            }
        }
        // 删除 session
        SessionManager.removeSessionByToken(tokens);
        // 删除各用户 SessionPerson 中对应的 token
        tokensByLogin.forEach((loginId, tokenList) -> {
            SessionPerson person = SessionManager.findPerson(loginId);
            if (person != null) {
                person.removeTokens(tokenList);
                SessionManager.storePerson(person);
            }
        });
    }

    private void setPerson() {
        if (this.person != null) {
            return;
        }
        this.person = SessionManager.findPerson(this.loginId);
    }

}
