package dev.simpleframework.token.path;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenPathConfig;
import dev.simpleframework.token.constant.HttpMethod;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.context.ContextRequest;
import dev.simpleframework.token.exception.InvalidContextException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class PathManager {
    /**
     * 路径前缀，实际要匹配的上下文请求路径将裁剪掉该前缀
     */
    private static String pathPrefix;
    /**
     * 所有自定义路径方法执行器
     */
    private static List<PathActionExecutor> CUSTOM_ACTION_EXECUTOR = PathActionBuilder.DEFAULT.init();

    public static void registerActionBuilder(PathActionBuilder builder) {
        if (builder == null) {
            return;
        }
        CUSTOM_ACTION_EXECUTOR = builder.init();
    }

    /**
     * 设置路径前缀
     */
    public static void setPathPrefix(String contextPath, String frameworkPath) {
        Function<String, String> parsePath = path -> {
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
        };

        String prefix = parsePath.apply(contextPath);
        prefix = prefix + parsePath.apply(frameworkPath);
        if (prefix.isBlank() || "/".equals(prefix)) {
            return;
        }
        pathPrefix = prefix;
    }

    /**
     * 执行路径方法
     * 1. 获取当前上下文请求路径和方法
     * 2. 请求路径裁剪掉前缀
     * 3，依次执行匹配的方法
     */
    public static void execAction() {
        execAction(buildConfigActionExecutors());
        execAction(CUSTOM_ACTION_EXECUTOR);
    }

    private static void execAction(List<PathActionExecutor> executors) {
        if (executors.isEmpty()) {
            return;
        }
        ContextRequest request = ContextManager.findContext().request();
        String requestPath = request.getPath();
        String requestMethod = request.getMethod();
        if (requestPath == null) {
            throw new InvalidContextException("Can not found the request path");
        }
        requestPath = cutPath(requestPath);

        // options 请求不执行路径方法
        boolean permitOptions = SimpleTokens.getGlobalConfig().getPath().getPermitOptionsRequest();
        if (permitOptions && HttpMethod.OPTIONS.name().equals(requestMethod)) {
            return;
        }

        for (PathActionExecutor executor : executors) {
            PathActionExecutor.Action handler = executor.getAction();
            if (handler == null) {
                continue;
            }
            List<PathInfo> includes = executor.getIncludes();
            List<PathInfo> excludes = executor.getExcludes();
            boolean match = (includes.isEmpty() || anyMatchPath(request, requestPath, requestMethod, includes))
                    && (excludes.isEmpty() || !anyMatchPath(request, requestPath, requestMethod, excludes));
            if (match) {
                handler.run();
            }
        }
    }

    private static List<PathActionExecutor> buildConfigActionExecutors() {
        SimpleTokenPathConfig config = SimpleTokens.getGlobalConfig().getPath();
        PathActionExecutor configExecutor = PathActionExecutor.of()
                // 不匹配不需要鉴权的路径才执行回调
                .notMatchInfo(config.getAllPermitPaths())
                .action(() -> {
                    // 校验登录
                    SimpleTokens.checkLogin();

                    // 校验权限
                    List<PathActionExecutor> permissionExecutors = new ArrayList<>();
                    PathActionExecutor permissionExecutor;
                    for (PathPermission permission : config.getPermissions()) {
                        permissionExecutor = PathActionExecutor.of()
                                .anyMatchMethod(permission.getPath(), permission.getHttpMethods())
                                .action(() -> {
                                    SimpleTokens.checkAnyPermission(permission.getPermissions());
                                    SimpleTokens.checkAnyRole(permission.getRoles());
                                });
                        permissionExecutors.add(permissionExecutor);
                    }
                    execAction(permissionExecutors);
                });
        return Collections.singletonList(configExecutor);
    }

    private static String cutPath(String path) {
        if (pathPrefix == null || pathPrefix.isBlank() || "/".equals(pathPrefix)) {
            return path;
        }
        if (path.startsWith(pathPrefix)) {
            return path.substring(pathPrefix.length());
        }
        return path;
    }

    private static boolean anyMatchPath(ContextRequest request, String path, String method, List<PathInfo> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        for (PathInfo pattern : patterns) {
            if (HttpMethod.contains(pattern.getHttpMethods(), httpMethod)
                    && request.matchPath(pattern.getPath(), path)) {
                return true;
            }
        }
        return false;
    }

}
