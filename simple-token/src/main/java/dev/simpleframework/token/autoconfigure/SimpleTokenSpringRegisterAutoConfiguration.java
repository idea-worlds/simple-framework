package dev.simpleframework.token.autoconfigure;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.context.SimpleTokenFrameworkContext;
import dev.simpleframework.token.context.SimpleTokenRpcContext;
import dev.simpleframework.token.path.PathActionBuilder;
import dev.simpleframework.token.path.PathManager;
import dev.simpleframework.token.permission.PermissionManager;
import dev.simpleframework.token.permission.PermissionQuery;
import dev.simpleframework.token.session.SessionGenerator;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SessionStore;
import dev.simpleframework.token.user.UserAccountPasswordValidator;
import dev.simpleframework.token.user.UserManager;
import dev.simpleframework.token.user.UserQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@AutoConfigureAfter(SimpleTokenSpringRedisAutoConfiguration.class)
@Order(Ordered.LOWEST_PRECEDENCE - 90)
public class SimpleTokenSpringRegisterAutoConfiguration {

    public SimpleTokenSpringRegisterAutoConfiguration(SimpleTokenConfig config) {
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
    public void setPathActionBuilder(PathActionBuilder builder) {
        PathManager.registerActionBuilder(builder);
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

}
