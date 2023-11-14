package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.context.SimpleTokenFrameworkContext;
import dev.simpleframework.token.context.impl.SpringReactorContext;
import dev.simpleframework.token.filter.SimpleTokenFilter;
import dev.simpleframework.token.filter.impl.SimpleTokenSpringReactorFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@AutoConfigureAfter(SimpleTokenSpringRegisterAutoConfiguration.class)
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class SimpleTokenSpringWebfluxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SimpleTokenFrameworkContext.class)
    public SimpleTokenFrameworkContext simpleTokenFrameworkContext() {
        return new SpringReactorContext();
    }

    @Bean
    @ConditionalOnMissingBean(SimpleTokenFilter.class)
    public SimpleTokenFilter simpleTokenDefaultFilter() {
        return new SimpleTokenSpringReactorFilter();
    }

    @Bean
    public WebFilter simpleTokenGlobalFilter() {
        return new SimpleTokenGlobalFilter();
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public static class SimpleTokenGlobalFilter implements WebFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            return chain.filter(exchange)
                    .doFinally(r -> {
                        SimpleTokens.clearThreadCache();
                    });
        }

    }

}
