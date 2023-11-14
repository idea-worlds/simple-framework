package dev.simpleframework.token.session;

/**
 * 会话值存储器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface SessionStore {

    /**
     * 存储会话值
     *
     * @param session 会话值
     */
    void setSession(SessionInfo session);

    /**
     * 获取会话值
     *
     * @param token 会话 token
     * @return 会话值
     */
    SessionInfo getSession(String token);

    /**
     * 清除 session
     *
     * @param token token
     */
    void removeSession(String token);

    /**
     * 存储应用会话值
     *
     * @param accountType 账号类型
     * @param loginId     登录 id
     * @param apps        应用会话值
     */
    void setApps(String accountType, String loginId, SimpleTokenApps apps);

    /**
     * 获取应用会话值
     *
     * @param accountType 账号类型
     * @param loginId     登录 id
     * @return 应用会话值
     */
    SimpleTokenApps getApps(String accountType, String loginId);

    /**
     * 清楚应用会话值
     *
     * @param accountType 账号类型
     * @param loginId     登录 id
     */
    void removeApps(String accountType, String loginId);

}
