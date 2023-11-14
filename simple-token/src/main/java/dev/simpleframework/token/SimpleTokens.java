package dev.simpleframework.token;

import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.exception.InvalidTokenException;
import dev.simpleframework.token.exception.NotPermissionException;
import dev.simpleframework.token.exception.NotRoleException;
import dev.simpleframework.token.exception.SimpleTokenException;
import dev.simpleframework.token.login.*;
import dev.simpleframework.token.permission.SimpleTokenPermission;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SimpleTokenApps;
import dev.simpleframework.token.session.SimpleTokenKickout;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * simple-token 权限认证工具类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Slf4j
public final class SimpleTokens {
    private static final ThreadLocal<SessionInfo> THREAD_LOCAL_SESSION = new InheritableThreadLocal<>();
    private static final ThreadLocal<SimpleTokenPermission> THREAD_LOCAL_PERMISSION = new InheritableThreadLocal<>();

    private static SimpleTokenConfig CONFIG = null;

    private SimpleTokens() {
    }

    public static void setGlobalConfig(SimpleTokenConfig config) {
        CONFIG = config;
    }

    public static SimpleTokenConfig getGlobalConfig() {
        return CONFIG;
    }

    /**
     * 获取当前会话的 token
     */
    public static String getToken() {
        return ContextManager.findToken();
    }

    /**
     * 获取当前会话值，未登录时抛异常
     */
    public static SessionInfo getSession() {
        SessionInfo session = THREAD_LOCAL_SESSION.get();
        if (session == null) {
            String token = getToken();
            session = SessionManager.findSession(token);
            if (session == null) {
                throw new InvalidTokenException("not login");
            }
            THREAD_LOCAL_SESSION.set(session);
        }
        return session;
    }

    /**
     * 获取当前登录的账号类型
     *
     * @return 账号类型
     */
    public static String getLoginAccountType() {
        return getSession().getAccountType();
    }

    /**
     * 获取当前登录的用户 id
     *
     * @return 用户 id
     */
    public static String getLoginId() {
        return getSession().getLoginId();
    }

    /**
     * 获取当前登录的用户 id 并转为 long 类型
     *
     * @return 用户 id
     */
    public static long getLoginIdAsLong() {
        return Long.parseLong(getLoginId());
    }

    /**
     * 获取当前登录的用户名
     *
     * @return 用户名
     */
    public static String getLoginUserName() {
        return getSession().getUserName();
    }

    /**
     * 当前会话是否已登录
     *
     * @return true: 已登录； false: 未登录
     */
    public static boolean isLogin() {
        String token = getToken();
        SessionInfo session = SessionManager.findSession(token);
        return session != null;
    }

    /**
     * 指定账号是否已登录
     *
     * @param accountType 账号类型
     * @param loginId     用户 id
     * @return true: 已登录； false: 未登录
     */
    public static boolean isLogin(String accountType, String loginId) {
        SimpleTokenApps apps = SessionManager.findApps(accountType, loginId);
        return apps != null && System.currentTimeMillis() < apps.findLastExpiredTime();
    }

    /**
     * 当前会话是否已登录，未登录时抛异常
     */
    public static void checkLogin() {
        getSession();
    }

    /**
     * 会话登录
     * 请在登录前自行校验账号密码
     *
     * @param id 用户 id
     * @return token
     */
    public static LoginResponse login(Long id) {
        LoginSetting config = new LoginSetting();
        return login(id, config);
    }

    /**
     * 会话登录
     * 请在登录前自行校验账号密码
     *
     * @param id 用户 id
     * @return token
     */
    public static LoginResponse login(String id) {
        LoginSetting config = new LoginSetting();
        return login(id, config);
    }

    /**
     * 会话登录，并指定登录的有效期
     * 请在登录前自行校验账号密码
     *
     * @param id      用户 id
     * @param timeout 本次登录的有效期
     * @return token
     */
    public static LoginResponse login(Long id, Duration timeout) {
        LoginSetting config = new LoginSetting();
        config.setTimeout(timeout);
        return login(id, config);
    }

    /**
     * 会话登录，并指定登录的有效期
     * 请在登录前自行校验账号密码
     *
     * @param id      用户 id
     * @param timeout 本次登录的有效期
     * @return token
     */
    public static LoginResponse login(String id, Duration timeout) {
        LoginSetting config = new LoginSetting();
        config.setTimeout(timeout);
        return login(id, config);
    }

    /**
     * 会话登录，并指定所有登录参数
     * 请在登录前自行校验账号密码
     *
     * @param id     用户 id
     * @param config 登录的参数
     * @return token
     */
    public static LoginResponse login(Long id, LoginSetting config) {
        if (id == null) {
            throw new SimpleTokenException("Login id can not be null");
        }
        return login(id.toString(), config);
    }

    /**
     * 会话登录，并指定所有登录参数
     * 请在登录前自行校验账号密码
     *
     * @param id     用户 id
     * @param config 登录的参数
     * @return token
     */
    public static LoginResponse login(String id, LoginSetting config) {
        // 构建一个登录领域对象执行登录逻辑
        SimpleTokenLogin login = new SimpleTokenLogin(id, config);
        login.exec();

        // 踢出本次登录后改账号过期的 token
        try {
            List<String> expiredTokens = login.getExpiredTokens();
            new SimpleTokenKickout().execByToken(expiredTokens);
        } catch (Exception e) {
            log.warn("kick out tokens error after login", e);
        }
        SessionInfo session = login.getSession();
        return new LoginResponse(session.getToken(), session.getExpiredTime());
    }

    /**
     * 根据账号密码进行会话登录
     * 账号名、账号密码错误会抛异常
     *
     * @param account  账号名
     * @param password 账号密码
     * @return token
     */
    public static LoginResponse loginByAccount(String account, String password) {
        return loginByAccount(account, password, new LoginSetting());
    }

    /**
     * 根据账号密码进行会话登录，并指定所有登录参数
     * 账号名、账号密码错误会抛异常
     *
     * @param account  账号名
     * @param password 账号密码
     * @param config   登录的参数
     * @return token
     */
    public static LoginResponse loginByAccount(String account, String password, LoginSetting config) {
        String accountType = config.getAccountType();
        UserAccount userAccount = UserManager.findAccountByName(accountType, account);
        UserManager.validatePassword(accountType, password, userAccount.getPassword());
        return login(userAccount.getId(), config);
    }

    /**
     * 登出（注销当前上下文中对应的登录信息）
     */
    public static void logout() {
        // 构建一个登出领域对象执行登出逻辑
        new SimpleTokenLogout().exec();
    }

    /**
     * 登出（注销用户的所有登录信息）
     *
     * @param accountType 账号类型
     * @param id          用户 id
     */
    public static void logout(String accountType, Long id) {
        if (id == null) {
            throw new SimpleTokenException("Login id can not be null");
        }
        logout(accountType, id.toString());
    }

    /**
     * 登出（注销用户的所有登录信息）
     *
     * @param accountType 账号类型
     * @param id          用户 id
     */
    public static void logout(String accountType, String id) {
        // 构建一个登出领域对象执行登出逻辑
        new SimpleTokenLogout(accountType, id).exec();
    }

    /**
     * 登出（注销用户在某个应用的所有登录信息）
     *
     * @param accountType 账号类型
     * @param id          用户 id
     * @param app         应用
     */
    public static void logout(String accountType, Long id, String app) {
        if (id == null) {
            throw new SimpleTokenException("Login id can not be null");
        }
        logout(accountType, id.toString(), app);
    }

    /**
     * 登出（注销用户在某个应用的所有登录信息）
     *
     * @param accountType 账号类型
     * @param id          用户 id
     * @param app         应用
     */
    public static void logout(String accountType, String id, String app) {
        // 构建一个登出领域对象执行登出逻辑
        new SimpleTokenLogout(accountType, id, app).exec();
    }

    /**
     * 踢用户下线
     *
     * @param accountType 账号类型
     * @param id          用户 id
     */
    public static void kickout(String accountType, Long id) {
        if (id == null) {
            throw new SimpleTokenException("Login id can not be null");
        }
        kickout(accountType, id.toString());
    }

    /**
     * 踢用户下线
     *
     * @param accountType 账号类型
     * @param id          用户 id
     */
    public static void kickout(String accountType, String id) {
        new SimpleTokenKickout(accountType, id).exec();
    }

    /**
     * 踢用户下线
     *
     * @param accountType 账号类型
     * @param id          用户 id
     * @param app         应用
     */
    public static void kickout(String accountType, Long id, String app) {
        if (id == null) {
            throw new SimpleTokenException("Login id can not be null");
        }
        kickout(accountType, id.toString(), app);
    }

    /**
     * 踢用户下线
     *
     * @param accountType 账号类型
     * @param id          用户 id
     * @param app         应用
     */
    public static void kickout(String accountType, String id, String app) {
        new SimpleTokenKickout(accountType, id, app).exec();
    }

    /**
     * 根据 token 踢对应的用户下线
     *
     * @param token token
     */
    public static void kickoutByToken(String token) {
        new SimpleTokenKickout().execByToken(Collections.singletonList(token));
    }

    /**
     * 获取当前账号所拥有的权限集合
     *
     * @return 权限集合
     */
    public static List<String> getPermissions() {
        return findPermission().getPermissions();
    }

    /**
     * 判断当前账号是否有指定权限
     *
     * @param permission 权限
     */
    public static boolean hasPermission(String permission) {
        return findPermission().hasPermission(permission);
    }

    /**
     * 校验当前账号是否有指定的所有权限，无权限抛异常 NotPermissionException
     *
     * @param permission 权限
     */
    public static void checkHasPermission(String... permission) {
        if (permission == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (!perm.hasPermission(permission)) {
            throw new NotPermissionException(perm.getNotMatch());
        }
    }

    /**
     * 校验当前账号是否有指定的所有权限，无权限抛异常 NotPermissionException
     *
     * @param permissions 权限
     */
    public static void checkHasPermission(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        checkHasPermission(permissions.toArray(new String[0]));
    }

    /**
     * 校验当前账号是否有指定的任一权限，无权限抛异常 NotPermissionException
     *
     * @param permission 权限
     */
    public static void checkAnyPermission(String... permission) {
        if (permission == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (!perm.anyPermission(permission)) {
            throw new NotPermissionException(perm.getNotMatch());
        }
    }

    /**
     * 校验当前账号是否有指定的任一权限，无权限抛异常 NotPermissionException
     *
     * @param permissions 权限
     */
    public static void checkAnyPermission(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        checkAnyPermission(permissions.toArray(new String[0]));
    }

    /**
     * 获取当前账号所拥有的角色集合
     *
     * @return 角色集合
     */
    public static List<String> getRoles() {
        return findPermission().getRoles();
    }

    /**
     * 判断当前账号是否有指定角色
     *
     * @param role 角色
     */
    public static boolean hasRole(String role) {
        return findPermission().hasRole(role);
    }

    /**
     * 校验当前账号是否有指定的所有角色，无权限抛异常 NotRoleException
     *
     * @param role 角色
     */
    public static void checkHasRole(String... role) {
        if (role == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (!perm.hasRole(role)) {
            throw new NotRoleException(perm.getNotMatch());
        }
    }

    /**
     * 校验当前账号是否有指定的所有角色，无权限抛异常 NotRoleException
     *
     * @param roles 角色
     */
    public static void checkHasRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        checkHasRole(roles.toArray(new String[0]));
    }

    /**
     * 校验当前账号是否有指定的任一角色，无权限抛异常 NotRoleException
     *
     * @param role 角色
     */
    public static void checkAnyRole(String... role) {
        if (role == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (!perm.anyRole(role)) {
            throw new NotRoleException(perm.getNotMatch());
        }
    }

    /**
     * 校验当前账号是否有指定的任一角色，无权限抛异常 NotRoleException
     *
     * @param roles 角色
     */
    public static void checkAnyRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        checkAnyRole(roles.toArray(new String[0]));
    }

    public static void clearThreadCache() {
        THREAD_LOCAL_SESSION.remove();
        THREAD_LOCAL_PERMISSION.remove();
    }

    private static SimpleTokenPermission findPermission() {
        SimpleTokenPermission permission = THREAD_LOCAL_PERMISSION.get();
        if (permission == null) {
            SessionInfo session = getSession();
            permission = SimpleTokenPermission.of(session.getAccountType(), session.getLoginId());
            THREAD_LOCAL_PERMISSION.set(permission);
        }
        return permission;
    }

}
