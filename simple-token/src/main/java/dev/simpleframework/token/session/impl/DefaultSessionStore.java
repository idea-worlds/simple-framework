package dev.simpleframework.token.session.impl;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class DefaultSessionStore extends AbstractSessionStore {
    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    @Override
    protected void set(String key, Object value, Duration timeout) {
        System.out.println("这只是个示例，不建议在生产环境使用 " + this.getClass());
        CACHE.put(key, value);
    }

    @Override
    protected <T> T get(String key, Class<T> clazz) {
        System.out.println("这只是个示例，不建议在生产环境使用 " + this.getClass());
        return (T) CACHE.get(key);
    }

    @Override
    protected void remove(String key) {
        System.out.println("这只是个示例，不建议在生产环境使用 " + this.getClass());
        CACHE.remove(key);
    }

}
