package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.context.SimpleTokenFrameworkContext;
import dev.simpleframework.token.context.impl.SpringServletContext;
import dev.simpleframework.token.filter.SimpleTokenFilter;
import dev.simpleframework.token.filter.impl.SimpleTokenSpringServletFilter;
import jakarta.servlet.*;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(SimpleTokenSpringRegisterAutoConfiguration.class)
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class SimpleTokenSpringWebmvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SimpleTokenFrameworkContext.class)
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
    public SimpleTokenFrameworkContext simpleTokenFrameworkContext() {
        return new SpringServletContext();
    }

    @Bean
    @ConditionalOnMissingBean(SimpleTokenFilter.class)
    @ConditionalOnClass(name = "jakarta.servlet.Filter")
    public SimpleTokenFilter simpleTokenDefaultFilter() {
        return new SimpleTokenSpringServletFilter();
    }

    @Bean
    public Filter simpleTokenGlobalFilter() {
        return new SimpleTokenGlobalFilter();
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public static class SimpleTokenGlobalFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            try {
                chain.doFilter(request, response);
            } finally {
                SimpleTokens.clearThreadCache();
            }
        }

    }

}
