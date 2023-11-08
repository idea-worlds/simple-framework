package dev.simpleframework.token.path;

import dev.simpleframework.core.EmptyFunction;
import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenPathConfig;
import dev.simpleframework.token.constant.HttpMethod;
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
     * 所有自定义路径匹配器
     */
    private static final List<PathMatcher> CUSTOM_MATCHERS = new CopyOnWriteArrayList<>();
    /**
     * 所有配置的路径匹配器
     */
    private static final List<PathMatcher> CONFIG_MATCHERS = new ArrayList<>();

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
     * 1. 获取当前上下文请求路径和方法
     * 2. 请求路径裁剪掉前缀
     * 3，依次执行匹配的回调
     */
    public static void execMatchers() {
        if (SimpleTokenPathConfig.hasChanged()) {
            setConfigMatchers();
        }
        SimpleTokenContext context = ContextManager.findContext();
        String requestPath = context.getRequestPath();
        String requestMethod = context.getRequestMethod();
        if (requestPath == null) {
            throw new InvalidContextException("Can not found the request path");
        }
        requestPath = parsePath(requestPath);
        List<PathMatcher> matchers = new ArrayList<>();
        matchers.addAll(CONFIG_MATCHERS);
        matchers.addAll(CUSTOM_MATCHERS);
        for (PathMatcher matcher : matchers) {
            EmptyFunction handler = matcher.getHandler();
            if (handler == null) {
                continue;
            }
            List<PathInfo> includes = matcher.getIncludes();
            List<PathInfo> excludes = matcher.getExcludes();
            boolean match = (includes.isEmpty() || anyMatchPath(context, requestPath, requestMethod, includes))
                    && (excludes.isEmpty() || !anyMatchPath(context, requestPath, requestMethod, excludes));
            if (match) {
                handler.run();
            }
        }
    }

    public synchronized static void setConfigMatchers() {
        CONFIG_MATCHERS.clear();
        SimpleTokenPathConfig config = SimpleTokens.getGlobalConfig().getPath();

        PathMatcher matcher = new PathMatcher();
        CONFIG_MATCHERS.add(
                matcher
                        // 不需要鉴权的路径：不匹配才校验是否登录
                        .notMatchInfo(config.getAllPermitPaths())
                        .handler(SimpleTokens::checkLogin)
        );
        SimpleTokenPathConfig.markChange(false);
    }

    /**
     * 添加路径匹配器
     *
     * @param matcher 路径匹配器
     */
    static void addMatcher(PathMatcher matcher) {
        CUSTOM_MATCHERS.add(matcher);
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

    private static boolean anyMatchPath(SimpleTokenContext context, String path, String method, List<PathInfo> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        for (PathInfo pattern : patterns) {
            if (HttpMethod.contains(pattern.getHttpMethods(), httpMethod)
                    && context.matchPath(pattern.getPath(), path)) {
                return true;
            }
        }
        return false;
    }

}
