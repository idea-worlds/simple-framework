package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.SimpleTokenContextForFramework;
import dev.simpleframework.token.exception.InvalidContextException;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Spring reactor 上下文处理器
 * 使用前需要在全局过滤器或者拦截器内先调用 setContext(exchange) 初始化当前线程的上下文，
 * 并在该全局过滤器或者拦截器最后调用 clearContext() 清除当前线程的上下文
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class SpringReactorContext
        extends AbstractSimpleTokenContext<ServerHttpRequest, ServerHttpResponse, ServerWebExchange>
        implements SimpleTokenContextForFramework {

    private static final ThreadLocal<ServerWebExchange> threadLocal = new InheritableThreadLocal<>();

    /**
     * 写入上下文对象，使用完毕必须清除 {@link #clearContext}
     */
    public static void setContext(ServerWebExchange exchange) {
        threadLocal.set(exchange);
    }

    /**
     * 获取当前线程的上下文对象
     */
    public static ServerWebExchange getContext() {
        ServerWebExchange context = threadLocal.get();
        if (context == null) {
            throw new InvalidContextException("Can not found a valid context");
        }
        return context;
    }

    /**
     * 清除当前线程的上下文对象
     */
    public static void clearContext() {
        threadLocal.remove();
    }

    @Override
    protected ServerHttpRequest getRequest() {
        return getContext().getRequest();
    }

    @Override
    protected ServerHttpResponse getResponse() {
        return getContext().getResponse();
    }

    @Override
    protected ServerWebExchange getStore() {
        return getContext();
    }

    @Override
    protected String getParam(ServerHttpRequest request, String key) {
        return request.getQueryParams().getFirst(key);
    }

    @Override
    protected String getHeader(ServerHttpRequest request, String key) {
        return request.getHeaders().getFirst(key);
    }

    @Override
    protected String getCookie(ServerHttpRequest request, String key) {
        HttpCookie cookie = request.getCookies().getFirst(key);
        if (cookie == null) {
            return null;
        }
        return cookie.getValue();
    }

    @Override
    protected void addHeader(ServerHttpResponse response, String key, String value) {
        response.getHeaders().add(key, value);
    }

    @Override
    protected Object get(ServerWebExchange store, String key) {
        return store.getAttributes().get(key);
    }

    @Override
    protected void set(ServerWebExchange store, String key, Object value) {
        store.getAttributes().put(key, value);
    }

    @Override
    protected void remove(ServerWebExchange store, String key) {
        store.getAttributes().remove(key);
    }

    @Override
    public boolean matchPath(String pattern, String path) {
        PathPattern pathPattern = PathPatternParser.defaultInstance.parse(pattern);
        PathContainer pathContainer = PathContainer.parsePath(path);
        return pathPattern.matches(pathContainer);
    }

    @Override
    public boolean enable() {
        return threadLocal.get() != null;
    }

}
