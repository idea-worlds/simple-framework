package dev.simpleframework.token.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.session.SessionStore;
import dev.simpleframework.token.session.impl.SpringRedisDefaultSessionStore;
import dev.simpleframework.token.session.impl.SpringRedisFastjsonSessionStore;
import dev.simpleframework.util.Jsons;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
@Order(Ordered.LOWEST_PRECEDENCE - 80)
public class SimpleTokenSpringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SimpleTokenConfig.class)
    @ConfigurationProperties(prefix = "simple.token")
    public SimpleTokenConfig simpleTokenConfig() {
        return new SimpleTokenConfig();
    }

    @Bean
    @ConditionalOnMissingBean(SessionStore.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    public SessionStore simpleTokenSessionStore(RedisConnectionFactory connectionFactory) {
        // 优先使用 jackson 序列化
        if (Jsons.jacksonExist()) {
            ObjectMapper objectMapper = Jsons.objectMapper(true);
            RedisSerializer<Object> valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
            RedisTemplate<String, Object> template = buildRedisTemplate(connectionFactory, valueSerializer);
            return new SpringRedisDefaultSessionStore(template);
        }
        // 再次使用 fastjson 序列化
        else if (Jsons.fastjsonExist()) {
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
