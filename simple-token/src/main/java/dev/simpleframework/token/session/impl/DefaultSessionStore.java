package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionPerson;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Slf4j
public class DefaultSessionStore extends AbstractSessionStore {
    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    @Override
    protected void setInfoData(String key, SessionInfo value, Duration timeout) {
        log.error("This is just an example and is not recommended for use in prod {}", this.getClass());
        CACHE.put(key, value);
    }

    @Override
    protected void setPersonData(String key, SessionPerson value, Duration timeout) {
        log.error("This is just an example and is not recommended for use in prod {}", this.getClass());
        CACHE.put(key, value);
    }

    @Override
    protected SessionInfo getInfoData(String key) {
        log.error("This is just an example and is not recommended for use in prod {}", this.getClass());
        return (SessionInfo) CACHE.get(key);
    }

    @Override
    protected SessionPerson getPersonData(String key) {
        log.error("This is just an example and is not recommended for use in prod {}", this.getClass());
        return (SessionPerson) CACHE.get(key);
    }

    @Override
    protected void remove(String key) {
        log.error("This is just an example and is not recommended for use in prod {}", this.getClass());
        CACHE.remove(key);
    }

}
