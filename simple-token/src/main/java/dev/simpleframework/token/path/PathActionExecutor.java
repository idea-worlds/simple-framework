package dev.simpleframework.token.path;

import dev.simpleframework.token.constant.HttpMethod;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 路径方法执行器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
public class PathActionExecutor {

    /**
     * 要匹配的路径通配符，其中之一匹配即可
     */
    private final List<PathInfo> includes = new ArrayList<>();
    /**
     * 要排除匹配的路径通配符
     */
    private final List<PathInfo> excludes = new ArrayList<>();
    /**
     * 匹配成功后执行的方法
     */
    private Action action;

    public static PathActionExecutor of() {
        return new PathActionExecutor();
    }

    public PathActionExecutor anyMatch(String... patterns) {
        if (patterns != null) {
            List<PathInfo> paths = Arrays.stream(patterns).map(PathInfo::new).toList();
            this.includes.addAll(paths);
        }
        return this;
    }

    public PathActionExecutor anyMatch(List<String> patterns) {
        if (patterns != null) {
            List<PathInfo> paths = patterns.stream().map(PathInfo::new).toList();
            this.includes.addAll(paths);
        }
        return this;
    }

    public PathActionExecutor anyMatchInfo(PathInfo... paths) {
        if (paths != null) {
            this.includes.addAll(Arrays.asList(paths));
        }
        return this;
    }

    public PathActionExecutor anyMatchInfo(List<PathInfo> paths) {
        if (paths != null) {
            this.includes.addAll(paths);
        }
        return this;
    }

    public PathActionExecutor anyMatchMethod(String pattern, HttpMethod... methods) {
        if (pattern != null) {
            this.includes.add(new PathInfo(pattern, methods));
        }
        return this;
    }

    public PathActionExecutor anyMatchMethod(String pattern, List<HttpMethod> methods) {
        if (pattern != null) {
            this.includes.add(new PathInfo(pattern, methods));
        }
        return this;
    }

    public PathActionExecutor notMatch(String... patterns) {
        if (patterns != null) {
            List<PathInfo> paths = Arrays.stream(patterns).map(PathInfo::new).toList();
            this.excludes.addAll(paths);
        }
        return this;
    }

    public PathActionExecutor notMatch(List<String> patterns) {
        if (patterns != null) {
            List<PathInfo> paths = patterns.stream().map(PathInfo::new).toList();
            this.excludes.addAll(paths);
        }
        return this;
    }

    public PathActionExecutor notMatchInfo(PathInfo... paths) {
        if (paths != null) {
            this.excludes.addAll(Arrays.asList(paths));
        }
        return this;
    }

    public PathActionExecutor notMatchInfo(List<PathInfo> paths) {
        if (paths != null) {
            this.excludes.addAll(paths);
        }
        return this;
    }

    public PathActionExecutor notMatchMethod(String pattern, HttpMethod... methods) {
        if (pattern != null) {
            this.excludes.add(new PathInfo(pattern, methods));
        }
        return this;
    }

    public PathActionExecutor notMatchMethod(String pattern, List<HttpMethod> methods) {
        if (pattern != null) {
            this.excludes.add(new PathInfo(pattern, methods));
        }
        return this;
    }

    public PathActionExecutor action(Action action) {
        this.action = action;
        return this;
    }

    @FunctionalInterface
    public interface Action {
        void run();
    }

}
