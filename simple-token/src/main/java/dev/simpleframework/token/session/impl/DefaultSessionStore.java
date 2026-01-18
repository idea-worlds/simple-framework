package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionPerson;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class DefaultSessionStore extends AbstractSessionStore {
    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    @Override
    protected void setInfoData(String key, SessionInfo value, Duration timeout) {
        System.out.println("这只是个示例，不建议在生产环境使用 " + this.getClass());
        CACHE.put(key, value);
    }

    @Override
    protected void setPersonData(String key, SessionPerson value, Duration timeout) {
        System.out.println("这只是个示例，不建议在生产环境使用 " + this.getClass());
        CACHE.put(key, value);
    }

    @Override
    protected SessionInfo getInfoData(String key) {
        System.out.println("这只是个示例，不建议在生产环境使用 " + this.getClass());
        return (SessionInfo) CACHE.get(key);
    }

    @Override
    protected SessionPerson getPersonData(String key) {
        System.out.println("这只是个示例，不建议在生产环境使用 " + this.getClass());
        return (SessionPerson) CACHE.get(key);
    }

    @Override
    protected void remove(String key) {
        System.out.println("这只是个示例，不建议在生产环境使用 " + this.getClass());
        CACHE.remove(key);
    }

}
