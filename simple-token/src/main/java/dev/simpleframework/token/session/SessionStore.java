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
     * 存储用户所有会话值
     *
     * @param loginId 登录 id
     * @param person  会话值
     */
    void setPerson(String loginId, SessionPerson person);

    /**
     * 获取用户所有会话值
     *
     * @param loginId 登录 id
     * @return 应用会话值
     */
    SessionPerson getPerson(String loginId);

    /**
     * 清除用户所有会话值
     *
     * @param loginId 登录 id
     */
    void removePerson(String loginId);

}
