package dev.simpleframework.token.session.impl;

import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SpringRedisFastjsonSessionStore extends AbstractSessionStore {
    private final RedisTemplate<String, String> template;

    public SpringRedisFastjsonSessionStore(RedisTemplate<String, String> template) {
        this.template = template;
    }

    @Override
    protected void set(String key, Object value, Duration timeout) {
        String val = JSON.toJSONString(value);
        if (timeout.toMillis() < 0) {
            this.template.opsForValue().set(key, val);
        } else {
            this.template.opsForValue().set(key, val, timeout);
        }
    }

    @Override
    protected <T> T get(String key, Class<T> clazz) {
        String result = this.template.opsForValue().get(key);
        return result == null ? null : JSON.parseObject(result, clazz);
    }

    @Override
    protected void remove(String key) {
        this.template.delete(key);
    }

}
