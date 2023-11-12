package dev.simpleframework.token.config;

import dev.simpleframework.token.path.PathInfo;
import dev.simpleframework.token.path.PathPermission;
import lombok.Data;

import java.util.*;

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
     * 不同账号类型的个性化路径权限，key 为 账号类型，未配置时取默认值 {@link #permissions}
     */
    private Map<String, List<PathPermission>> accountPermissions = new HashMap<>();

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

    /**
     * 获取账号类型对应的权限配置，未配置时取默认值 {@link #permissions}
     *
     * @param accountType 账号类型
     * @return 路径权限集
     */
    public List<PathPermission> findPermission(String accountType) {
        List<PathPermission> permissions = this.accountPermissions.get(accountType);
        if (permissions == null) {
            permissions = this.permissions;
        }
        return permissions;
    }

}
