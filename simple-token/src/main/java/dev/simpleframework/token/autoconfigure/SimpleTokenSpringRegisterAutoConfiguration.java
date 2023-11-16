package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.context.SimpleTokenFrameworkContext;
import dev.simpleframework.token.context.SimpleTokenRpcContext;
import dev.simpleframework.token.login.UserManager;
import dev.simpleframework.token.login.UserAccountPasswordValidator;
import dev.simpleframework.token.login.UserQuery;
import dev.simpleframework.token.path.PathManager;
import dev.simpleframework.token.permission.PermissionManager;
import dev.simpleframework.token.permission.PermissionQuery;
import dev.simpleframework.token.session.SessionGenerator;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SessionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@RequiredArgsConstructor
@AutoConfigureAfter(SimpleTokenSpringAutoConfiguration.class)
@Order(Ordered.LOWEST_PRECEDENCE - 90)
public class SimpleTokenSpringRegisterAutoConfiguration implements InitializingBean {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.mvc.servlet.path:}")
    private String servletPath;

    @Autowired
    public void setConfig(SimpleTokenConfig config) {
        SimpleTokens.setGlobalConfig(config);
    }

    @Autowired(required = false)
    public void setFrameworkContext(SimpleTokenFrameworkContext context) {
        ContextManager.registerFrameworkContext(context);
    }

    @Autowired(required = false)
    public void setRpcContext(List<SimpleTokenRpcContext> contexts) {
        ContextManager.registerRpcContext(contexts);
    }

    @Autowired(required = false)
    public void setUserQuery(UserQuery query) {
        UserManager.registerQuery(query);
    }

    @Autowired(required = false)
    public void setUserAccountPasswordValidator(UserAccountPasswordValidator validator) {
        UserManager.registerPasswordValidator(validator);
    }

    @Autowired(required = false)
    public void setPermissionQuery(PermissionQuery query) {
        PermissionManager.registerQuery(query);
    }

    @Autowired(required = false)
    public void setSessionStore(SessionStore store) {
        SessionManager.registerStore(store);
    }

    @Autowired(required = false)
    public void setSessionGenerator(SessionGenerator generator) {
        SessionManager.registerGenerator(generator);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setPathPrefix();
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
