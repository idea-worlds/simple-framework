package dev.simpleframework.token.permission;

import dev.simpleframework.token.exception.ImplementationNotFoundException;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class PermissionManager {
    private static PermissionStore STORE = PermissionStore.DEFAULT;

    public static void registerStore(PermissionStore store) {
        STORE = store;
    }

    /**
     * 获取账号的权限集
     *
     * @param accountType 账号类型
     * @param loginId     用户 id
     * @return 权限集
     */
    public static List<String> listPermissions(String accountType, String loginId) {
        validStore();
        return STORE.listPermissions(accountType, loginId);
    }

    /**
     * 获取账号的角色集
     *
     * @param accountType 账号类型
     * @param loginId     用户 id
     * @return 角色集
     */
    public static List<String> listRoles(String accountType, String loginId) {
        validStore();
        return STORE.listRoles(accountType, loginId);
    }

    private static void validStore() {
        if (STORE == null) {
            throw new ImplementationNotFoundException(PermissionStore.class, PermissionManager.class);
        }
    }

}
