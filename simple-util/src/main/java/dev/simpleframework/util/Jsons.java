package dev.simpleframework.util;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;

import java.io.File;
import java.util.function.Consumer;

/**
 * JSON 工具类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public final class Jsons {

    private static boolean absent;
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
            absent = false;
        } catch (Throwable e) {
            absent = true;
        }
    }

    public static boolean present() {
        return !absent;
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
    public static <T> T read(String json, Class<T> clazz) {
        validPresent();

        return OBJECT_MAPPER.readValue(json, clazz);
    }

    @SneakyThrows
    public static <T> T read(File src, Class<T> clazz) {
        validPresent();

        return OBJECT_MAPPER.readValue(src, clazz);
    }

    @SneakyThrows
    public static <T> String write(T json) {
        validPresent();

        return OBJECT_MAPPER.writeValueAsString(json);
    }

    @SneakyThrows
    public static void write(File out, Object value) {
        validPresent();

        OBJECT_MAPPER.writeValue(out, value);
    }

    public static void config(Consumer<ObjectMapper> action) {
        action.accept(OBJECT_MAPPER);
    }

    private static void validPresent() throws ClassNotFoundException {
        if (absent) {
            throw new ClassNotFoundException("No dependency found for json");
        }
    }

}
