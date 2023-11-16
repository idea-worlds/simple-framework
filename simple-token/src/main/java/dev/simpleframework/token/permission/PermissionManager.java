package dev.simpleframework.token.permission;

import dev.simpleframework.token.exception.ImplementationNotFoundException;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class PermissionManager {
    private static PermissionQuery QUERY = PermissionQuery.DEFAULT;

    public static void registerQuery(PermissionQuery query) {
        QUERY = query;
    }

    /**
     * 获取用户的权限集
     *
     * @param loginId 用户 id
     * @return 权限集
     */
    public static List<String> listPermissions(String loginId) {
        validStore();
        return QUERY.listPermissions(loginId);
    }

    /**
     * 获取用户的角色集
     *
     * @param loginId 用户 id
     * @return 角色集
     */
    public static List<String> listRoles(String loginId) {
        validStore();
        return QUERY.listRoles(loginId);
    }

    private static void validStore() {
        if (QUERY == null) {
            throw new ImplementationNotFoundException(PermissionQuery.class, PermissionManager.class);
        }
    }

}
