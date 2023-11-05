package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.context.SimpleTokenContextForFramework;
import dev.simpleframework.token.context.impl.SpringServletContext;
import dev.simpleframework.token.filter.SimpleTokenFilter;
import dev.simpleframework.token.filter.impl.SimpleTokenSpringServletFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class SimpleTokenSpringWebmvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SimpleTokenContextForFramework.class)
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
    public SimpleTokenContextForFramework simpleTokenContextForFramework() {
        return new SpringServletContext();
    }

    @Bean
    @ConditionalOnMissingBean(SimpleTokenFilter.class)
    @ConditionalOnClass(name = "jakarta.servlet.Filter")
    public SimpleTokenFilter simpleTokenFilter() {
        return new SimpleTokenSpringServletFilter();
    }

}
