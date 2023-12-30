package dev.simpleframework.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * JSON 工具类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public final class Jsons {

    private static boolean jacksonExist = false;
    private static boolean fastjsonExist = false;
    private static ObjectMapper OBJECT_MAPPER;

    static {
        setJackson();
        setFastjson();
    }

    public static boolean present() {
        return !jacksonExist && !fastjsonExist;
    }

    public static boolean jacksonExist() {
        return jacksonExist;
    }

    public static boolean fastjsonExist() {
        return fastjsonExist;
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
        if (jacksonExist) {
            return OBJECT_MAPPER.readValue(json, clazz);
        } else if (fastjsonExist) {
            return JSON.parseObject(json, clazz);
        }
        throw new ClassNotFoundException("No dependency found for json");
    }

    @SneakyThrows
    public static <T> T read(File src, Class<T> clazz) {
        if (jacksonExist) {
            return OBJECT_MAPPER.readValue(src, clazz);
        } else if (fastjsonExist) {
            return JSON.parseObject(src.toURI().toURL(), clazz);
        }
        throw new ClassNotFoundException("No dependency found for json");
    }

    @SneakyThrows
    public static <T> T readWithGeneric(String json, Class<T> clazz, Class<?>... components) {
        if (jacksonExist) {
            JavaType type = OBJECT_MAPPER.getTypeFactory().constructParametricType(clazz, components);
            return OBJECT_MAPPER.readValue(json, type);
        } else if (fastjsonExist) {
            Type type = TypeReference.parametricType(clazz, components);
            return JSON.parseObject(json, type);
        }
        throw new ClassNotFoundException("No dependency found for json");
    }

    @SneakyThrows
    public static String write(Object json) {
        if (jacksonExist) {
            return OBJECT_MAPPER.writeValueAsString(json);
        } else if (fastjsonExist) {
            return JSON.toJSONString(json);
        }
        throw new ClassNotFoundException("No dependency found for json");
    }

    @SneakyThrows
    public static void write(Object json, File out) {
        if (jacksonExist) {
            OBJECT_MAPPER.writeValue(out, json);
        } else if (fastjsonExist) {
            try (OutputStream outputStream = new FileOutputStream(out)) {
                JSON.writeTo(outputStream, json);
            }
        }
        throw new ClassNotFoundException("No dependency found for json");
    }

    public static void config(Consumer<ObjectMapper> action) {
        action.accept(OBJECT_MAPPER);
    }

    private static void setJackson() {
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

    private static void setFastjson() {
        try {
            Class.forName("com.alibaba.fastjson2.JSON");
            fastjsonExist = true;
        } catch (Throwable e) {
            fastjsonExist = false;
        }
    }

}
