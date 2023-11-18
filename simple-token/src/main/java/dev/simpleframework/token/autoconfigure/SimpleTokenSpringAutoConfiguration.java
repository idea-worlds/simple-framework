package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.config.SimpleTokenConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

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

}
