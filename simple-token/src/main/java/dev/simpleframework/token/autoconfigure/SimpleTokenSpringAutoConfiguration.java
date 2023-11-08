package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.config.SimpleTokenConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
public class SimpleTokenSpringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SimpleTokenConfig.class)
    @ConfigurationProperties(prefix = "simple.token")
    public SimpleTokenConfig simpleTokenConfig() {
        return new SimpleTokenConfig();
    }

}
