package dev.simpleframework.token.context;

/**
 * 上下文存储器对象
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface ContextStore {

    void set(String key, Object value);

    <T> T get(String key);

    void remove(String key);

}
