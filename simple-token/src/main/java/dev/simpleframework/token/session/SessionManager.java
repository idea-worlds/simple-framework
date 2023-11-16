package dev.simpleframework.token.session;

import dev.simpleframework.token.exception.ImplementationNotFoundException;
import dev.simpleframework.token.session.impl.DefaultSessionGenerator;
import dev.simpleframework.token.session.impl.DefaultSessionStore;

import java.util.Collection;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class SessionManager {
    private static SessionGenerator GENERATOR = new DefaultSessionGenerator();
    private static SessionStore STORE = new DefaultSessionStore();

    /**
     * 注册会话值生成器
     *
     * @param generator 会话值生成器
     */
    public static void registerGenerator(SessionGenerator generator) {
        GENERATOR = generator;
    }

    /**
     * 注册会话值存储器
     *
     * @param store 会话值存储器
     */
    public static void registerStore(SessionStore store) {
        STORE = store;
    }

    /**
     * 根据 token 查询会话值
     *
     * @param token token
     * @return 会话值
     */
    public static SessionInfo findSession(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        validStore();
        return STORE.getSession(token);
    }

    /**
     * 构建一个新的会话值
     *
     * @param loginId     登录 id
     * @param expiredTime 过期时间
     * @return 会话值
     */
    public static SessionInfo createSession(String loginId, String userName, long expiredTime) {
        validGenerator();
        SessionInfo session = new SessionInfo(loginId, userName, expiredTime);
        return GENERATOR.generate(session);
    }

    /**
     * 存储会话值
     *
     * @param session 会话值
     */
    public static void storeSession(SessionInfo session) {
        validStore();
        STORE.setSession(session);
    }

    /**
     * 根据 token 清除会话值
     *
     * @param token token
     */
    public static void removeSessionByToken(String token) {
        validStore();
        STORE.removeSession(token);
    }

    /**
     * 根据 token 清除会话值
     *
     * @param tokens token
     */
    public static void removeSessionByToken(Collection<String> tokens) {
        if (tokens == null) {
            return;
        }
        validStore();
        for (String token : tokens) {
            STORE.removeSession(token);
        }
    }

    /**
     * 查找用户的所有会话值
     *
     * @param loginId 登录 id
     * @return 应用会话值
     */
    public static SessionPerson findPerson(String loginId) {
        validStore();
        SessionPerson person = STORE.getPerson(loginId);
        if (person != null) {
            // 清除过期的数据
            person.removeExpired();
        }
        return person;
    }

    /**
     * 存储用户的所有会话值
     *
     * @param loginId 登录 id
     * @param person  会话值
     */
    public static void storePerson(String loginId, SessionPerson person) {
        validStore();
        STORE.setPerson(loginId, person);
    }

    /**
     * 删除用户的所有会话值
     *
     * @param loginId 登录 id
     */
    public static void removePerson(String loginId) {
        validStore();
        STORE.removePerson(loginId);
    }

    private static void validGenerator() {
        if (GENERATOR == null) {
            throw new ImplementationNotFoundException(SessionGenerator.class, SessionManager.class);
        }
    }

    private static void validStore() {
        if (STORE == null) {
            throw new ImplementationNotFoundException(SessionStore.class, SessionManager.class);
        }
    }

}
