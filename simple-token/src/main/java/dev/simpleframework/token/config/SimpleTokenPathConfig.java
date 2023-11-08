package dev.simpleframework.token.config;

import dev.simpleframework.token.path.PathInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 路径配置
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SimpleTokenPathConfig {
    private static volatile boolean CHANGED = false;

    /**
     * 公共路径
     */
    private List<String> publicPaths = Arrays.asList("/public/**", "/error/**");
    /**
     * 静态路径
     */
    private List<String> staticPaths = Arrays.asList("/css/**", "/js/**", "/images/**", "/webjars/**", "/**/favicon.ico", "/static/**");
    /**
     * 是否不鉴权所有公共路径
     */
    private Boolean permitPublic = Boolean.TRUE;
    /**
     * 是否不鉴权所有静态路径
     */
    private Boolean permitStatic = Boolean.TRUE;
    /**
     * 是否不鉴权所有 options 请求
     */
    private Boolean permitOptionsRequest = Boolean.TRUE;
    /**
     * 不需要鉴权的路径
     */
    private List<PathInfo> permit;

    public List<PathInfo> getAllPermitPaths() {
        List<PathInfo> result = new ArrayList<>();
        if (this.permitPublic) {
            result.addAll(this.publicPaths.stream().map(PathInfo::new).toList());
        }
        if (this.permitStatic) {
            result.addAll(this.staticPaths.stream().map(PathInfo::new).toList());
        }
        if (this.permit != null) {
            result.addAll(this.permit);
        }
        return result;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
        markChange(true);
    }

    public void setStaticPaths(List<String> staticPaths) {
        this.staticPaths = staticPaths;
        markChange(true);
    }

    public void setPermitPublic(Boolean permitPublic) {
        this.permitPublic = permitPublic;
        markChange(true);
    }

    public void setPermitStatic(Boolean permitStatic) {
        this.permitStatic = permitStatic;
        markChange(true);
    }

    public void setPermitOptionsRequest(Boolean permitOptionsRequest) {
        this.permitOptionsRequest = permitOptionsRequest;
        markChange(true);
    }

    public void setPermit(List<PathInfo> permit) {
        this.permit = permit;
        markChange(true);
    }

    public static void markChange(boolean flag) {
        CHANGED = flag;
    }

    public static boolean hasChanged() {
        return CHANGED;
    }

}
