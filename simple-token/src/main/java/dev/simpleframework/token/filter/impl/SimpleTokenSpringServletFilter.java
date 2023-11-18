package dev.simpleframework.token.filter.impl;

import dev.simpleframework.token.filter.SimpleTokenFilter;
import dev.simpleframework.token.path.PathManager;
import jakarta.servlet.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class SimpleTokenSpringServletFilter implements SimpleTokenFilter, Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.beforeAuth(request, response);
        this.doAuth(request, response);
        this.afterAuth(request, response);
        chain.doFilter(request, response);
    }

    protected void beforeAuth(ServletRequest request, ServletResponse response) {
        // nothing
    }

    protected void doAuth(ServletRequest request, ServletResponse response) {
        PathManager.execMatchers();
    }

    protected void afterAuth(ServletRequest request, ServletResponse response) {
        // nothing
    }

}
