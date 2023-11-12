package dev.simpleframework.token.path;

import dev.simpleframework.token.constant.HttpMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 路径权限
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
@Setter
@ToString
public class PathPermission extends PathInfo {

    /**
     * 该路径下必须有的权限集
     */
    private List<String> permissions = new ArrayList<>();
    /**
     * 该路径下必须有的角色集
     */
    private List<String> roles = new ArrayList<>();

    public PathPermission() {
        super();
    }

    public PathPermission(String path, HttpMethod... methods) {
        super(path, methods);
    }

    public PathPermission(String path, List<HttpMethod> methods) {
        super(path, methods);
    }

    public PathPermission setPermissions(List<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public PathPermission setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public PathPermission addPermission(String permission) {
        this.permissions.add(permission);
        return this;
    }

    public PathPermission addRole(String role) {
        this.roles.add(role);
        return this;
    }

}
