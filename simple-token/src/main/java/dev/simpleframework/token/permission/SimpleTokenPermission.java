package dev.simpleframework.token.permission;

import dev.simpleframework.util.Strings;
import lombok.Getter;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenPermission {

    private final String accountType;
    private final String loginId;
    private List<String> permissions;
    private List<String> roles;
    private boolean foundPermissions = false;
    private boolean foundRoles = false;
    @Getter
    private String notMatch = "";

    public static SimpleTokenPermission of(String accountType, String loginId) {
        return new SimpleTokenPermission(accountType, loginId);
    }

    public List<String> getPermissions() {
        this.setPermissions();
        return this.permissions;
    }

    public List<String> getRoles() {
        this.setRoles();
        return this.roles;
    }

    public boolean hasPermission(String... permissions) {
        if (permissions == null) {
            return false;
        }
        this.setPermissions();
        for (String permission : permissions) {
            if (!match(this.permissions, permission)) {
                this.notMatch = permission;
                return false;
            }
        }
        return true;
    }

    public boolean anyPermission(String... permissions) {
        if (permissions == null) {
            return false;
        }
        this.setPermissions();
        for (String permission : permissions) {
            if (match(this.permissions, permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRole(String... roles) {
        if (roles == null) {
            return false;
        }
        this.setRoles();
        for (String role : roles) {
            if (!match(this.roles, role)) {
                this.notMatch = role;
                return false;
            }
        }
        return true;
    }

    public boolean anyRole(String... roles) {
        if (roles == null) {
            return false;
        }
        this.setRoles();
        for (String role : roles) {
            if (match(this.roles, role)) {
                return true;
            }
        }
        return false;
    }

    private void setPermissions() {
        if (this.foundPermissions) {
            return;
        }
        this.permissions = PermissionManager.listPermissions(this.accountType, this.loginId);
        this.foundPermissions = true;
    }

    private void setRoles() {
        if (this.foundRoles) {
            return;
        }
        this.roles = PermissionManager.listRoles(this.accountType, this.loginId);
        this.foundRoles = true;
    }

    private static boolean match(List<String> list, String element) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        if (list.contains(element)) {
            return true;
        }
        // 模糊匹配，即 * 表示全部
        for (String pattern : list) {
            if (Strings.like('*', pattern, element)) {
                return true;
            }
        }
        return false;
    }

    private SimpleTokenPermission(String accountType, String loginId) {
        this.accountType = accountType;
        this.loginId = loginId;
    }

}
