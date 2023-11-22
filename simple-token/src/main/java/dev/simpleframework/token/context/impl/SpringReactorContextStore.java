package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@RequiredArgsConstructor
public class SpringReactorContextStore implements ContextStore {
    private final ServerWebExchange store;

    @Override
    public void set(String key, Object value) {
        this.store.getAttributes().put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) this.store.getAttributes().get(key);
    }

    @Override
    public void remove(String key) {
        this.store.getAttributes().remove(key);
    }

}
