package dev.simpleframework.token.permission;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.annotation.TokenCheckPermission;
import dev.simpleframework.token.annotation.TokenCheckRole;
import dev.simpleframework.token.exception.ImplementationNotFoundException;

import java.lang.reflect.Method;
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
     * 获取当前用户的权限集
     *
     * @return 权限集
     */
    public static List<String> findPermissions() {
        validQuery();
        return QUERY.listPermissions();
    }

    /**
     * 获取当前用户的角色集
     *
     * @return 角色集
     */
    public static List<String> findRoles() {
        validQuery();
        return QUERY.listRoles();
    }

    /**
     * 校验方法上的注解权限
     */
    public static void checkAnnotation(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        checkAnnotation(clazz.getAnnotation(TokenCheckPermission.class));
        checkAnnotation(clazz.getAnnotation(TokenCheckRole.class));
        checkAnnotation(method.getAnnotation(TokenCheckPermission.class));
        checkAnnotation(method.getAnnotation(TokenCheckRole.class));
    }

    private static void checkAnnotation(TokenCheckPermission permission) {
        if (permission == null) {
            return;
        }
        switch (permission.mode()) {
            case ANY -> SimpleTokens.checkAnyPermission(permission.value());
            case ALL -> SimpleTokens.checkHasPermission(permission.value());
            case NOT -> SimpleTokens.checkNotPermission(permission.value());
        }
    }

    private static void checkAnnotation(TokenCheckRole role) {
        if (role == null) {
            return;
        }
        switch (role.mode()) {
            case ANY -> SimpleTokens.checkAnyRole(role.value());
            case ALL -> SimpleTokens.checkHasRole(role.value());
            case NOT -> SimpleTokens.checkNotRole(role.value());
        }
    }

    private static void validQuery() {
        if (QUERY == null) {
            throw new ImplementationNotFoundException(PermissionQuery.class, PermissionManager.class);
        }
    }

}
