package dev.simpleframework.token.path;

import dev.simpleframework.core.EmptyFunction;
import dev.simpleframework.token.constant.HttpMethod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 路径匹配器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PathMatcher {

    /**
     * 要匹配的路径通配符，其中之一匹配即可
     */
    private final List<PathInfo> includes = new ArrayList<>();
    /**
     * 要排除匹配的路径通配符
     */
    private final List<PathInfo> excludes = new ArrayList<>();
    /**
     * 匹配成功后执行的回调
     */
    private EmptyFunction handler;

    public static PathMatcher create() {
        PathMatcher matcher = new PathMatcher();
        PathManager.addMatcher(matcher);
        return matcher;
    }

    public static PathMatcher match(String pattern, EmptyFunction handler) {
        PathMatcher matcher = new PathMatcher()
                .anyMatch(pattern)
                .handler(handler);
        PathManager.addMatcher(matcher);
        return matcher;
    }

    public static PathMatcher match(String pattern, HttpMethod method, EmptyFunction handler) {
        PathMatcher matcher = new PathMatcher()
                .anyMatchMethod(pattern, method)
                .handler(handler);
        PathManager.addMatcher(matcher);
        return matcher;
    }

    public static PathMatcher match(String pattern, List<HttpMethod> methods, EmptyFunction handler) {
        PathMatcher matcher = new PathMatcher()
                .anyMatchMethod(pattern, methods)
                .handler(handler);
        PathManager.addMatcher(matcher);
        return matcher;
    }

    public PathMatcher anyMatch(String... patterns) {
        if (patterns != null) {
            List<PathInfo> paths = Arrays.stream(patterns).map(PathInfo::new).toList();
            this.includes.addAll(paths);
        }
        return this;
    }

    public PathMatcher anyMatch(List<String> patterns) {
        if (patterns != null) {
            List<PathInfo> paths = patterns.stream().map(PathInfo::new).toList();
            this.includes.addAll(paths);
        }
        return this;
    }

    public PathMatcher anyMatchInfo(List<PathInfo> paths) {
        if (paths != null) {
            this.includes.addAll(paths);
        }
        return this;
    }

    public PathMatcher anyMatchMethod(String pattern, HttpMethod... methods) {
        if (pattern != null) {
            this.includes.add(new PathInfo(pattern, methods));
        }
        return this;
    }

    public PathMatcher anyMatchMethod(String pattern, List<HttpMethod> methods) {
        if (pattern != null) {
            this.includes.add(new PathInfo(pattern, methods));
        }
        return this;
    }

    public PathMatcher notMatch(String... patterns) {
        if (patterns != null) {
            List<PathInfo> paths = Arrays.stream(patterns).map(PathInfo::new).toList();
            this.excludes.addAll(paths);
        }
        return this;
    }

    public PathMatcher notMatch(List<String> patterns) {
        if (patterns != null) {
            List<PathInfo> paths = patterns.stream().map(PathInfo::new).toList();
            this.excludes.addAll(paths);
        }
        return this;
    }

    public PathMatcher notMatchInfo(List<PathInfo> paths) {
        if (paths != null) {
            this.excludes.addAll(paths);
        }
        return this;
    }

    public PathMatcher notMatchMethod(String pattern, HttpMethod... methods) {
        if (pattern != null) {
            this.excludes.add(new PathInfo(pattern, methods));
        }
        return this;
    }

    public PathMatcher notMatchMethod(String pattern, List<HttpMethod> methods) {
        if (pattern != null) {
            this.excludes.add(new PathInfo(pattern, methods));
        }
        return this;
    }

    public PathMatcher handler(EmptyFunction handler) {
        this.handler = handler;
        return this;
    }

}
