package dev.simpleframework.token.permission;

import java.util.Collections;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface PermissionStore {

    /**
     * 获取账号的权限集
     *
     * @param accountType 账号类型
     * @param loginId     用户 id
     * @return 权限集
     */
    List<String> listPermissions(String accountType, String loginId);

    /**
     * 获取账号的角色集
     *
     * @param accountType 账号类型
     * @param loginId     用户 id
     * @return 角色集
     */
    List<String> listRoles(String accountType, String loginId);


    PermissionStore DEFAULT = new PermissionStore() {
        @Override
        public List<String> listPermissions(String accountType, String loginId) {
            return Collections.emptyList();
        }

        @Override
        public List<String> listRoles(String accountType, String loginId) {
            return Collections.emptyList();
        }
    };

}
