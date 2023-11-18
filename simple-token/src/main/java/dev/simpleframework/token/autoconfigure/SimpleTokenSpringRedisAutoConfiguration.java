package dev.simpleframework.token.autoconfigure;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.simpleframework.token.session.SessionStore;
import dev.simpleframework.token.session.impl.SpringRedisDefaultSessionStore;
import dev.simpleframework.token.session.impl.SpringRedisFastjsonSessionStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.data.redis.connection.RedisConnectionFactory")
public class SimpleTokenSpringRedisAutoConfiguration {
    private static boolean jacksonExist;
    private static boolean fastjsonExist;

    static {
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            jacksonExist = true;
        } catch (Throwable e) {
            jacksonExist = false;
        }
        try {
            Class.forName("com.alibaba.fastjson2.JSON");
            fastjsonExist = true;
        } catch (Throwable e) {
            fastjsonExist = false;
        }
    }

    @Bean
    @ConditionalOnMissingBean(SessionStore.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    public SessionStore simpleTokenSessionStore(RedisConnectionFactory connectionFactory) {
        // 优先使用 jackson 序列化
        if (jacksonExist) {
            ObjectMapper objectMapper = JsonMapper.builder()
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
            RedisSerializer<Object> valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
            RedisTemplate<String, Object> template = buildRedisTemplate(connectionFactory, valueSerializer);
            return new SpringRedisDefaultSessionStore(template);
        }
        // 再次使用 fastjson 序列化
        else if (fastjsonExist) {
            RedisTemplate<String, String> template = new StringRedisTemplate(connectionFactory);
            return new SpringRedisFastjsonSessionStore(template);
        }
        // 都无 json 依赖时使用 jdk 序列化
        else {
            RedisSerializer<Object> valueSerializer = new JdkSerializationRedisSerializer();
            RedisTemplate<String, Object> template = buildRedisTemplate(connectionFactory, valueSerializer);
            return new SpringRedisDefaultSessionStore(template);
        }
    }

    private static RedisTemplate<String, Object> buildRedisTemplate(
            RedisConnectionFactory connectionFactory, RedisSerializer<Object> valueSerializer) {
        RedisSerializer<String> keySerializer = RedisSerializer.string();
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

}
