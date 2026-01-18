package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionPerson;
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
    protected void setInfoData(String key, SessionInfo value, Duration timeout) {
        if (timeout.toMillis() < 0) {
            this.template.opsForValue().set(key, value);
        } else {
            this.template.opsForValue().set(key, value, timeout);
        }
    }

    @Override
    protected void setPersonData(String key, SessionPerson value, Duration timeout) {
        if (timeout.toMillis() < 0) {
            this.template.opsForValue().set(key, value);
        } else {
            this.template.opsForValue().set(key, value, timeout);
        }
    }

    @Override
    protected SessionInfo getInfoData(String key) {
        return (SessionInfo) this.template.opsForValue().get(key);
    }

    @Override
    protected SessionPerson getPersonData(String key) {
        return (SessionPerson) this.template.opsForValue().get(key);
    }

    @Override
    protected void remove(String key) {
        this.template.delete(key);
    }

}
