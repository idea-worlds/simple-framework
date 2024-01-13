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
     * 获取当前用户的权限集
     *
     * @return 权限集
     */
    List<String> listPermissions();

    /**
     * 获取当前用户的角色集
     *
     * @return 角色集
     */
    List<String> listRoles();


    PermissionQuery DEFAULT = new PermissionQuery() {
        @Override
        public List<String> listPermissions() {
            return Collections.emptyList();
        }

        @Override
        public List<String> listRoles() {
            return Collections.emptyList();
        }
    };

}
