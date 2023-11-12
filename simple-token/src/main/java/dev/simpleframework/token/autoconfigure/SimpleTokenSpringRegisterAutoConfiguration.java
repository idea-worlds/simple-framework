package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.context.SimpleTokenContextForFramework;
import dev.simpleframework.token.context.SimpleTokenContextForRpc;
import dev.simpleframework.token.login.AccountManager;
import dev.simpleframework.token.login.AccountPasswordValidator;
import dev.simpleframework.token.login.AccountStore;
import dev.simpleframework.token.path.PathManager;
import dev.simpleframework.token.permission.PermissionManager;
import dev.simpleframework.token.permission.PermissionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.mvc.servlet.path:}")
    private String servletPath;

    @Autowired
    public void setConfig(SimpleTokenConfig config) {
        SimpleTokens.setGlobalConfig(config);
    }

    @Autowired(required = false)
    public void setContextForFramework(SimpleTokenContextForFramework context) {
        ContextManager.registerFrameworkContext(context);
    }

    @Autowired(required = false)
    public void setContextForRpc(SimpleTokenContextForRpc context) {
        ContextManager.registerRpcContext(context);
    }

    @Autowired(required = false)
    public void setAccountStore(AccountStore store) {
        AccountManager.registerStore(store);
    }

    @Autowired(required = false)
    public void setAccountPasswordValidator(AccountPasswordValidator validator) {
        AccountManager.registerValidator(validator);
    }

    @Autowired(required = false)
    public void setPermissionStore(PermissionStore store) {
        PermissionManager.registerStore(store);
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
