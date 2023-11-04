package dev.simpleframework.token.login;

import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SimpleTokenApps;
import dev.simpleframework.token.session.SimpleTokenSession;

import java.util.Collections;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenLogout {

    private String accountType;
    private String loginId;
    private String app;

    public SimpleTokenLogout() {
    }

    public SimpleTokenLogout(String accountType, String loginId) {
        this.accountType = accountType;
        this.loginId = loginId;
    }

    public SimpleTokenLogout(String accountType, String loginId, String app) {
        this.accountType = accountType;
        this.loginId = loginId;
        this.app = app;
    }

    public void exec() {
        if (this.accountType == null) {
            this.logoutByToken();
        }
        if (this.accountType != null && this.loginId != null) {
            this.logoutByApp();
        }

    }

    private void logoutByToken() {
        // 删除上下文中的 token
        String token = ContextManager.removeToken();
        if (token == null) {
            return;
        }
        // 获取 session
        SimpleTokenSession session = SessionManager.findSession(token);
        if (session == null) {
            return;
        }
        // 删除 session
        SessionManager.removeSessionByToken(token);

        // 删除应用会话中的 token
        String accountType = session.getAccountType();
        String loginId = session.getLoginId();
        SimpleTokenApps apps = SessionManager.findApps(accountType, loginId);
        if (apps != null) {
            apps.removeTokens(Collections.singletonList(token));
            SessionManager.storeApps(accountType, loginId, apps);
        }
    }

    private void logoutByApp() {
        SimpleTokenApps apps = SessionManager.findApps(this.accountType, this.loginId);
        if (apps == null) {
            // 查无应用会话，说明该账号未登录过
            return;
        }
        if (this.app == null) {
            List<String> tokens = apps.findAllTokens();
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除应用会话
            SessionManager.removeApps(this.accountType, this.loginId);
        } else {
            List<String> tokens = apps.findAllTokens(this.app);
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除应用会话中的 token
            apps.removeTokens(tokens);
            SessionManager.storeApps(this.accountType, this.loginId, apps);
        }
    }

}
