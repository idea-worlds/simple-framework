package dev.simpleframework.token.login;

import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SessionPerson;

import java.util.Collections;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenLogout {

    private String loginId;
    private String app;

    public SimpleTokenLogout() {
    }

    public SimpleTokenLogout(String loginId) {
        this.loginId = loginId;
    }

    public SimpleTokenLogout(String loginId, String app) {
        this.loginId = loginId;
        this.app = app;
    }

    public void exec() {
        if (this.loginId == null) {
            this.logoutByToken();
        } else {
            this.logoutByUserApp();
        }
    }

    private void logoutByToken() {
        // 删除上下文中的 token
        String token = ContextManager.removeToken();
        if (token == null) {
            return;
        }
        // 获取 session
        SessionInfo session = SessionManager.findSession(token);
        if (session == null) {
            return;
        }
        // 删除 session
        SessionManager.removeSessionByToken(token);

        // 删除用户所有会话中对应的 token
        String loginId = session.getLoginId();
        SessionPerson person = SessionManager.findPerson(loginId);
        if (person != null) {
            person.removeTokens(Collections.singletonList(token));
            SessionManager.storePerson(loginId, person);
        }
    }

    private void logoutByUserApp() {
        SessionPerson person = SessionManager.findPerson(this.loginId);
        if (person == null) {
            // 查无会话，说明该账号未登录过
            return;
        }
        if (this.app == null) {
            List<String> tokens = person.findAllTokens();
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除用户所有会话
            SessionManager.removePerson(this.loginId);
        } else {
            List<String> tokens = person.findAllTokens(this.app);
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除用户所有会话中对应的 token
            person.removeTokens(tokens);
            SessionManager.storePerson(this.loginId, person);
        }
    }

}
