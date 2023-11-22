package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextRequest;
import dev.simpleframework.token.context.ContextResponse;
import dev.simpleframework.token.context.ContextStore;
import dev.simpleframework.token.context.SimpleTokenFrameworkContext;
import org.springframework.web.server.ServerWebExchange;

/**
 * Spring reactor 上下文处理器
 * 使用前需要在全局过滤器或者拦截器内先调用 setContext(exchange) 初始化当前线程的上下文，
 * 并在该全局过滤器或者拦截器最后调用 clearContext() 清除当前线程的上下文
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class SpringReactorContext extends AbstractContext implements SimpleTokenFrameworkContext {

    private static final ThreadLocal<ServerWebExchange> threadLocal = new InheritableThreadLocal<>();

    /**
     * 写入上下文对象，使用完毕必须清除 {@link #clearContext}
     */
    public static void setContext(ServerWebExchange exchange) {
        ContextRequest contextRequest = new SpringReactorContextRequest(exchange.getRequest());
        ContextResponse contextResponse = new SpringReactorContextResponse(exchange.getResponse());
        ContextStore contextStore = new SpringReactorContextStore(exchange);
        AbstractContext.setContextData(contextRequest, contextResponse, contextStore);
    }

    /**
     * 清除当前线程的上下文对象
     */
    public static void clearContext() {
        AbstractContext.removeContextData();
    }

    @Override
    public boolean enable() {
        return threadLocal.get() != null;
    }

}
