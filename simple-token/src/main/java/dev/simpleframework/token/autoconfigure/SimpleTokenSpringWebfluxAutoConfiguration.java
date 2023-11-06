package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.context.SimpleTokenContextForFramework;
import dev.simpleframework.token.context.impl.SpringReactorContext;
import dev.simpleframework.token.filter.SimpleTokenFilter;
import dev.simpleframework.token.filter.impl.SimpleTokenSpringReactorFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class SimpleTokenSpringWebfluxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SimpleTokenContextForFramework.class)
    public SimpleTokenContextForFramework simpleTokenContextForFramework() {
        return new SpringReactorContext();
    }

    @Bean
    @ConditionalOnMissingBean(SimpleTokenFilter.class)
    public SimpleTokenFilter simpleTokenFilter() {
        return new SimpleTokenSpringReactorFilter();
    }

}
