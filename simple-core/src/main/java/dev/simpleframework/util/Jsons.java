package dev.simpleframework.util;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JSON 工具类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public final class Jsons {

    private static boolean jacksonExist = false;
    private static ObjectMapper OBJECT_MAPPER;

    static {
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");

            OBJECT_MAPPER = JsonMapper.builder()
                    .enable(
                            // 允许注释
                            JsonReadFeature.ALLOW_JAVA_COMMENTS,
                            JsonReadFeature.ALLOW_YAML_COMMENTS,
                            // 允许属性名没加双引号
                            JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES,
                            // 允许属性名和值用单引号
                            JsonReadFeature.ALLOW_SINGLE_QUOTES,
                            // 允许非引号控制字符（比如 \n）
                            JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                    // 使用 BigDecimal 序列化浮点数
                    .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                    // 忽略枚举大小写
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                    // 允许未知属性
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    // 允许空对象
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    .build();
            jacksonExist = true;
        } catch (Throwable e) {
            jacksonExist = false;
        }
    }

    public static boolean present() {
        return !jacksonExist;
    }

    public static void config(Consumer<ObjectMapper> action) {
        action.accept(OBJECT_MAPPER);
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    public static ObjectMapper objectMapper(boolean newInstance) {
        if (newInstance) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setConfig(OBJECT_MAPPER.getSerializationConfig());
            objectMapper.setConfig(OBJECT_MAPPER.getDeserializationConfig());
            return objectMapper;
        }
        return OBJECT_MAPPER;
    }

    @SneakyThrows
    public static <T> T read(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String json) {
            return OBJECT_MAPPER.readValue(json, clazz);
        }
        if (obj instanceof File src) {
            return OBJECT_MAPPER.readValue(src, clazz);
        }
        if (obj instanceof Reader src) {
            return OBJECT_MAPPER.readValue(src, clazz);
        }
        if (obj instanceof InputStream src) {
            return OBJECT_MAPPER.readValue(src, clazz);
        }
        if (obj instanceof URL src) {
            return OBJECT_MAPPER.readValue(src, clazz);
        }
        if (obj instanceof byte[] src) {
            return OBJECT_MAPPER.readValue(src, clazz);
        }
        return OBJECT_MAPPER.convertValue(obj, clazz);
    }

    public static Map<String, Object> toMap(Object obj) {
        return toMap(obj, String.class, Object.class);
    }

    public static <K, V> Map<K, V> toMap(Object obj, Class<K> keyClass, Class<V> valueClass) {
        MapType type = OBJECT_MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, keyClass, valueClass);
        return OBJECT_MAPPER.convertValue(obj, type);
    }

    @SneakyThrows
    public static <T> T readWithGeneric(String json, Class<T> clazz, Class<?>... components) {
        JavaType type = OBJECT_MAPPER.getTypeFactory().constructParametricType(clazz, components);
        return OBJECT_MAPPER.readValue(json, type);
    }

    @SneakyThrows
    public static String write(Object json) {
        return OBJECT_MAPPER.writeValueAsString(json);
    }

    @SneakyThrows
    public static void write(Object json, File out) {
        OBJECT_MAPPER.writeValue(out, json);
    }

    @SneakyThrows
    public static void write(Object json, OutputStream out) {
        OBJECT_MAPPER.writeValue(out, json);
    }

    @SneakyThrows
    public static void write(Object json, Writer out) {
        OBJECT_MAPPER.writeValue(out, json);
    }

}
