package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.context.SimpleTokenFrameworkContext;
import dev.simpleframework.token.context.impl.SpringServletContext;
import dev.simpleframework.token.filter.SimpleTokenFilter;
import dev.simpleframework.token.filter.impl.SimpleTokenSpringServletFilter;
import dev.simpleframework.token.path.PathManager;
import dev.simpleframework.token.permission.PermissionManager;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(SimpleTokenSpringRegisterAutoConfiguration.class)
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class SimpleTokenSpringWebmvcAutoConfiguration implements InitializingBean {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.mvc.servlet.path:}")
    private String servletPath;

    @Bean
    @ConditionalOnMissingBean(SimpleTokenFrameworkContext.class)
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
    public SimpleTokenFrameworkContext simpleTokenFrameworkContext() {
        return new SpringServletContext();
    }

    @Bean
    @ConditionalOnMissingBean(SimpleTokenFilter.class)
    @ConditionalOnClass(name = "jakarta.servlet.Filter")
    public Filter simpleTokenDefaultFilter() {
        return new SimpleTokenSpringServletFilter();
    }

    @Bean
    public Filter simpleTokenGlobalFilter() {
        return new SimpleTokenGlobalFilter();
    }

    @Bean
    public HandlerInterceptor simpleTokenGlobalInterceptor() {
        return new SimpleTokenGlobalInterceptor();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        PathManager.setPathPrefix(contextPath, servletPath);
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public static class SimpleTokenGlobalFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            try {
                SpringServletContext.setContext((HttpServletRequest) request, (HttpServletResponse) response);
                chain.doFilter(request, response);
            } finally {
                SpringServletContext.clearContext();
                SimpleTokens.clearThreadCache();
            }
        }

    }

    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    public static class SimpleTokenGlobalInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            if (handler instanceof HandlerMethod method) {
                PermissionManager.checkAnnotation(method.getMethod());
            }
            return true;
        }

    }

}
