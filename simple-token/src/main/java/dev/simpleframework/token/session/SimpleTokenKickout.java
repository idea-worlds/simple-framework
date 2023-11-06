package dev.simpleframework.token.session;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenKickout {
    private String accountType;
    private String loginId;
    private String app;
    private SimpleTokenApps apps;

    public SimpleTokenKickout() {
    }

    public SimpleTokenKickout(String accountType, String loginId) {
        this.accountType = accountType;
        this.loginId = loginId;
    }

    public SimpleTokenKickout(String accountType, String loginId, String app) {
        this.accountType = accountType;
        this.loginId = loginId;
        this.app = app;
    }

    public void exec() {
        this.setApps();
        if (this.apps == null) {
            return;
        }
        if (this.app == null) {
            List<String> tokens = this.apps.findAllTokens();
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除应用会话
            SessionManager.removeApps(this.accountType, this.loginId);
        } else {
            List<String> tokens = this.apps.findAllTokens(this.app);
            // 删除 session
            SessionManager.removeSessionByToken(tokens);
            // 删除应用会话中的 token
            this.apps.removeTokens(tokens);
            SessionManager.storeApps(this.accountType, this.loginId, this.apps);
        }
    }

    public void execByToken(List<String> tokens) {
        this.setApps();
        // 删除 session
        SessionManager.removeSessionByToken(tokens);
        // 删除应用会话中的 token
        if (this.apps != null) {
            this.apps.removeTokens(tokens);
            SessionManager.storeApps(this.accountType, this.loginId, this.apps);
        }
    }

    private void setApps() {
        if (this.accountType == null || this.apps != null) {
            return;
        }
        this.apps = SessionManager.findApps(this.accountType, this.loginId);
    }

}
