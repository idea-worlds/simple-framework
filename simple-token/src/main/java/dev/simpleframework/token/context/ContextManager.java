package dev.simpleframework.token.context;

import dev.simpleframework.token.exception.ImplementationNotFoundException;
import dev.simpleframework.token.exception.InvalidContextException;

/**
 * 上下文管理器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public final class ContextManager {

    public static SimpleTokenContextForFramework FRAMEWORK_CONTEXT = null;
    public static SimpleTokenContextForRpc RPC_CONTEXT = null;

    public static void registerFrameworkContext(SimpleTokenContextForFramework context) {
        if (context == null) {
            return;
        }
        FRAMEWORK_CONTEXT = context;
    }

    public static void registerRpcContext(SimpleTokenContextForRpc context) {
        if (context == null) {
            return;
        }
        RPC_CONTEXT = context;
    }

    /**
     * 获取上下文存储器
     *
     * @return 上下文存储器
     */
    public static SimpleTokenContext findContext() {
        if (FRAMEWORK_CONTEXT == null && RPC_CONTEXT == null) {
            throw new ImplementationNotFoundException(SimpleTokenContext.class, ContextManager.class);
        }
        // 先查是否有框架上下文且可用
        if (FRAMEWORK_CONTEXT != null && FRAMEWORK_CONTEXT.enable()) {
            return FRAMEWORK_CONTEXT;
        }
        // 再查是否有 rpc 上下文且可用
        if (RPC_CONTEXT != null && RPC_CONTEXT.enable()) {
            return RPC_CONTEXT;
        }
        // 查无上下文或都不可用时抛异常
        throw new InvalidContextException("Can not found a valid context");
    }

    /**
     * 获取 token
     */
    public static String findToken() {
        return findContext().getToken();
    }

    /**
     * 存储 token
     *
     * @param token       token
     * @param expiredTime 过期时间
     */
    public static void storeToken(String token, long expiredTime) {
        if (token == null || token.isBlank()) {
            return;
        }
        findContext().setToken(token, expiredTime);
    }

    /**
     * 删除 token
     */
    public static String removeToken() {
        return findContext().removeToken();
    }

}
