package dev.simpleframework.util;

import java.io.*;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JSON 工具类，运行时自动检测可用的 Jackson 版本：
 * 优先使用 Jackson 3.x（tools.jackson），降级到 Jackson 2.x（com.fasterxml.jackson）
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public final class Jsons {

    private static final JsonsDelegate DELEGATE;

    static {
        JsonsDelegate delegate = null;
        // 优先检测 Jackson 3.x（tools.jackson）
        try {
            Class.forName("tools.jackson.databind.ObjectMapper");
            delegate = new Jackson3JsonsDelegate();
        } catch (Throwable ignored) {
        }
        // 降级到 Jackson 2.x（com.fasterxml.jackson）
        if (delegate == null) {
            try {
                Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
                delegate = new Jackson2JsonsDelegate();
            } catch (Throwable ignored) {
            }
        }
        DELEGATE = delegate;
    }

    /**
     * 配置内部 ObjectMapper。
     * 调用方按自身 Jackson 版本声明 Consumer 类型，例如：
     * {@code Jsons.<tools.jackson.databind.ObjectMapper>config(m -> m.enable(...))}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void config(Consumer<T> action) {
        DELEGATE.config((Consumer) action);
    }

    /**
     * 返回内部 ObjectMapper 实例。
     * 调用方按自身 Jackson 版本接收，例如：
     * {@code tools.jackson.databind.ObjectMapper mapper = Jsons.objectMapper()}
     */
    @SuppressWarnings("unchecked")
    public static <T> T objectMapper() {
        return (T) DELEGATE.objectMapper();
    }

    /**
     * 返回 ObjectMapper 实例。
     *
     * @param newInstance true 时返回与内部实例配置相同的新实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T objectMapper(boolean newInstance) {
        return (T) DELEGATE.objectMapper(newInstance);
    }

    /**
     * 将对象反序列化为指定类型。
     * obj 支持：{@link String}、{@link File}、{@link Reader}、{@link InputStream}、
     * {@link java.net.URL}、{@code byte[]}，以及任意可转换对象（convertValue）。
     * obj 为 null 时返回 null。
     */
    public static <T> T read(Object obj, Class<T> clazz) {
        return DELEGATE.read(obj, clazz);
    }

    /**
     * 将对象转换为 {@code Map<String, Object>}。
     */
    public static Map<String, Object> toMap(Object obj) {
        return DELEGATE.toMap(obj);
    }

    /**
     * 将对象转换为指定键值类型的 Map。
     */
    public static <K, V> Map<K, V> toMap(Object obj, Class<K> keyClass, Class<V> valueClass) {
        return DELEGATE.toMap(obj, keyClass, valueClass);
    }

    /**
     * 将 JSON 字符串反序列化为带泛型参数的类型。
     * 例如反序列化 {@code List<User>}：
     * {@code Jsons.readWithGeneric(json, List.class, User.class)}
     */
    public static <T> T readWithGeneric(String json, Class<T> clazz, Class<?>... components) {
        return DELEGATE.readWithGeneric(json, clazz, components);
    }

    /**
     * 将对象序列化为 JSON 字符串。
     */
    public static String write(Object json) {
        return DELEGATE.write(json);
    }

    /**
     * 将对象序列化为 JSON 并写入文件。
     */
    public static void write(Object json, File out) {
        DELEGATE.write(json, out);
    }

    /**
     * 将对象序列化为 JSON 并写入输出流。
     */
    public static void write(Object json, OutputStream out) {
        DELEGATE.write(json, out);
    }

    /**
     * 将对象序列化为 JSON 并写入 Writer。
     */
    public static void write(Object json, Writer out) {
        DELEGATE.write(json, out);
    }

    interface JsonsDelegate {
        Object objectMapper();

        Object objectMapper(boolean newInstance);

        void config(Consumer<Object> action);

        <T> T read(Object obj, Class<T> clazz);

        Map<String, Object> toMap(Object obj);

        <K, V> Map<K, V> toMap(Object obj, Class<K> keyClass, Class<V> valueClass);

        <T> T readWithGeneric(String json, Class<T> clazz, Class<?>... components);

        String write(Object json);

        void write(Object json, File out);

        void write(Object json, OutputStream out);

        void write(Object json, Writer out);
    }

}
