package dev.simpleframework.token.filter.impl;

import dev.simpleframework.token.filter.SimpleTokenFilter;
import dev.simpleframework.token.path.PathManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class SimpleTokenSpringReactorFilter implements SimpleTokenFilter, WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        this.beforeAuth(exchange);
        this.doAuth(exchange);
        this.afterAuth(exchange);
        return chain.filter(exchange);
    }

    protected void beforeAuth(ServerWebExchange exchange) {
        // nothing
    }

    protected void doAuth(ServerWebExchange exchange) {
        PathManager.execMatchers();
    }

    protected void afterAuth(ServerWebExchange exchange) {
        // nothing
    }

}
