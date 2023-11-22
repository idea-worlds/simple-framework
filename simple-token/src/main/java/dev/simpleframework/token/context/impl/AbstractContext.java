package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextRequest;
import dev.simpleframework.token.context.ContextResponse;
import dev.simpleframework.token.context.ContextStore;
import dev.simpleframework.token.context.SimpleTokenContext;
import dev.simpleframework.token.exception.InvalidContextException;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class AbstractContext implements SimpleTokenContext {
    private static final ThreadLocal<Context> threadLocal = new InheritableThreadLocal<>();

    protected static void setContextData(ContextRequest request, ContextResponse response, ContextStore store) {
        Context context = new Context(request, response, store);
        threadLocal.set(context);
    }

    protected static Context getContextData() {
        return getContextData(true);
    }

    protected static Context getContextData(boolean throwException) {
        Context context = threadLocal.get();
        if (context == null && throwException) {
            throw new InvalidContextException("Can not found a valid context");
        }
        return context;
    }

    protected static void removeContextData() {
        threadLocal.remove();
    }

    @Override
    public ContextRequest request() {
        ContextRequest request = getContextData().request();
        if (request == null) {
            throw new InvalidContextException("Can not found ContextRequest");
        }
        return request;
    }

    @Override
    public ContextResponse response() {
        ContextResponse response = getContextData().response();
        if (response == null) {
            throw new InvalidContextException("Can not found ContextResponse");
        }
        return response;
    }

    @Override
    public ContextStore store() {
        ContextStore store = getContextData().store();
        if (store == null) {
            throw new InvalidContextException("Can not found ContextStore");
        }
        return store;
    }

    public record Context(ContextRequest request, ContextResponse response, ContextStore store) {
    }

}
