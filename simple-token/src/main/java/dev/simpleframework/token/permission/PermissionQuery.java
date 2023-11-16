package dev.simpleframework.token.permission;

import java.util.Collections;
import java.util.List;

/**
 * 权限查询器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface PermissionQuery {

    /**
     * 获取用户的权限集
     *
     * @param loginId 用户 id
     * @return 权限集
     */
    List<String> listPermissions(String loginId);

    /**
     * 获取用户的角色集
     *
     * @param loginId 用户 id
     * @return 角色集
     */
    List<String> listRoles(String loginId);


    PermissionQuery DEFAULT = new PermissionQuery() {
        @Override
        public List<String> listPermissions(String loginId) {
            return Collections.emptyList();
        }

        @Override
        public List<String> listRoles(String loginId) {
            return Collections.emptyList();
        }
    };

}
