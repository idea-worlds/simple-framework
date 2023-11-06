package dev.simpleframework.token.path;

import dev.simpleframework.core.EmptyFunction;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathMatcher {

    /**
     * 要匹配的路径通配符，其中之一匹配即可
     */
    private final List<String> includes = new ArrayList<>();
    /**
     * 要排除匹配的路径通配符
     */
    private final List<String> excludes = new ArrayList<>();
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

    public PathMatcher anyMatch(String... patterns) {
        if (patterns != null) {
            this.includes.addAll(Arrays.asList(patterns));
        }
        return this;
    }

    public PathMatcher notMatch(String... patterns) {
        if (patterns != null) {
            this.excludes.addAll(Arrays.asList(patterns));
        }
        return this;
    }

    public PathMatcher handler(EmptyFunction handler) {
        this.handler = handler;
        return this;
    }

}
