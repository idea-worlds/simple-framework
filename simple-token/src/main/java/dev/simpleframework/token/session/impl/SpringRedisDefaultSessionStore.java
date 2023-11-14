package dev.simpleframework.token.session.impl;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SpringRedisDefaultSessionStore extends AbstractSessionStore {
    private final RedisTemplate<String, Object> template;

    public SpringRedisDefaultSessionStore(RedisTemplate<String, Object> template) {
        this.template = template;
    }

    @Override
    protected void set(String key, Object value, Duration timeout) {
        if (timeout.toMillis() < 0) {
            this.template.opsForValue().set(key, value);
        } else {
            this.template.opsForValue().set(key, value, timeout);
        }
    }

    @Override
    protected <T> T get(String key, Class<T> clazz) {
        return (T) this.template.opsForValue().get(key);
    }

    @Override
    protected void remove(String key) {
        this.template.delete(key);
    }

}
