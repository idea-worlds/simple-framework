package dev.simpleframework.token.config;

import dev.simpleframework.token.path.PathInfo;
import dev.simpleframework.token.path.PathPermission;
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
    /**
     * 路径权限
     */
    private List<PathPermission> permissions = new ArrayList<>();

    /**
     * 获取所有不需要鉴权的路径
     *
     * @return 路径集
     */
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

}
