package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextStore;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@RequiredArgsConstructor
public class SpringServletContextStore implements ContextStore {
    private final HttpServletRequest store;

    @Override
    public void set(String key, Object value) {
        this.store.setAttribute(key, value);
    }

    @Override
    @SuppressWarnings(("unchecked"))
    public <T> T get(String key) {
        return (T) this.store.getAttribute(key);
    }

    @Override
    public void remove(String key) {
        this.store.removeAttribute(key);
    }

}
