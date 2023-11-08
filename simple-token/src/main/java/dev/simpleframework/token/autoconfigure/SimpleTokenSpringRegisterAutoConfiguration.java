package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.context.SimpleTokenContextForFramework;
import dev.simpleframework.token.path.PathManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@RequiredArgsConstructor
@Order
public class SimpleTokenSpringRegisterAutoConfiguration implements InitializingBean {
    private final SimpleTokenConfig config;
    private final SimpleTokenContextForFramework context;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.mvc.servlet.path:}")
    private String servletPath;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setPathPrefix();
        SimpleTokens.setGlobalConfig(this.config);
        ContextManager.registerFrameworkContext(context);
    }

    private void setPathPrefix() {
        String prefix = parsePath(contextPath);
        prefix = prefix + parsePath(servletPath);
        if (prefix.isBlank() || "/".equals(prefix)) {
            return;
        }
        PathManager.setPathPrefix(prefix);
    }

    /**
     * path 最前加 / 最后去掉 /
     */
    private static String parsePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

}
