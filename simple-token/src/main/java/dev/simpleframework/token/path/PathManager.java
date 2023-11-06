package dev.simpleframework.token.path;

import dev.simpleframework.core.EmptyFunction;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.context.SimpleTokenContext;
import dev.simpleframework.token.exception.InvalidContextException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class PathManager {
    /**
     * 路径前缀，实际要匹配的上下文请求路径将裁剪掉该前缀
     */
    private static String pathPrefix;
    /**
     * 所有路径匹配器
     */
    private static final List<PathMatcher> MATCHERS = new CopyOnWriteArrayList<>();

    /**
     * 设置路径前缀
     *
     * @param prefix 路径前缀
     */
    public static void setPathPrefix(String prefix) {
        pathPrefix = prefix;
    }

    /**
     * 执行路径匹配器
     * 1. 获取当前上下文请求路径
     * 2. 请求路径裁剪掉前缀
     * 3. 获取所有匹配请求路径的匹配器内的回调
     * 4，依次执行所有匹配的回调
     */
    public static void execMatchers() {
        SimpleTokenContext context = ContextManager.findContext();
        String requestPath = context.getRequestPath();
        if (requestPath == null) {
            throw new InvalidContextException("Can not found the request path");
        }
        requestPath = parsePath(requestPath);
        List<PathMatcher> matchers = MATCHERS.stream().toList();
        List<EmptyFunction> handlers = new ArrayList<>();
        for (PathMatcher matcher : matchers) {
            EmptyFunction handler = matcher.getHandler();
            if (handler == null) {
                continue;
            }
            boolean match = context.matchPath(matcher.getIncludes(), requestPath)
                    && !context.matchPath(matcher.getExcludes(), requestPath);
            if (match) {
                handlers.add(handler);
            }
        }
        for (EmptyFunction handler : handlers) {
            handler.run();
        }
    }

    /**
     * 添加路径匹配器
     *
     * @param matcher 路径匹配器
     */
    static void addMatcher(PathMatcher matcher) {
        MATCHERS.add(matcher);
    }

    private static String parsePath(String path) {
        if (pathPrefix == null || pathPrefix.isBlank() || "/".equals(pathPrefix)) {
            return path;
        }
        if (path.startsWith(pathPrefix)) {
            return path.substring(pathPrefix.length());
        }
        return path;
    }

}
