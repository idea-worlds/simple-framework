package dev.simpleframework.token;

import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.context.ContextManager;
import dev.simpleframework.token.exception.InvalidPermissionException;
import dev.simpleframework.token.exception.InvalidRoleException;
import dev.simpleframework.token.exception.InvalidTokenException;
import dev.simpleframework.token.exception.SimpleTokenException;
import dev.simpleframework.token.permission.SimpleTokenPermission;
import dev.simpleframework.token.session.*;
import dev.simpleframework.token.user.UserAccount;
import dev.simpleframework.token.user.UserManager;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * simple-token 权限认证工具类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Slf4j
public final class SimpleTokens {
    private static final ThreadLocal<SessionInfo> THREAD_LOCAL_SESSION = new InheritableThreadLocal<>();
    private static final ThreadLocal<SimpleTokenPermission> THREAD_LOCAL_PERMISSION = new InheritableThreadLocal<>();
    private static final List<String> REFRESH_TOKENS = new CopyOnWriteArrayList<>();

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
     * 重置当前会话值
     */
    public static void refreshSession() {
        SessionInfo session = getSession();
        String token = session.getToken();
        if (REFRESH_TOKENS.contains(token)) {
            return;
        }
        REFRESH_TOKENS.add(token);
        try {
            long expiredTime = getGlobalConfig().tokenExpiredTime();
            new SimpleTokenSessionRefresh(session, expiredTime).exec();
            // 存储 token 至上下文
            ContextManager.storeToken(session.getToken(), session.getExpiredTime());
        } finally {
            REFRESH_TOKENS.remove(token);
        }
    }

    /**
     * 重置用户的会话值
     *
     * @param loginId 用户 id
     */
    public static void refreshSession(String loginId) {
        new SimpleTokenSessionRefresh(loginId).exec();
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
     * 指定用户是否已登录
     *
     * @param loginId 用户 id
     * @return true: 已登录； false: 未登录
     */
    public static boolean isLogin(String loginId) {
        SessionPerson person = SessionManager.findPerson(loginId);
        return person != null && System.currentTimeMillis() < person.findLastExpiredTime();
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
    public static String login(Long id) {
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
    public static String login(String id) {
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
    public static String login(Long id, Duration timeout) {
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
    public static String login(String id, Duration timeout) {
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
    public static String login(Long id, LoginSetting config) {
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
    public static String login(String id, LoginSetting config) {
        // 构建一个登录领域对象执行登录逻辑
        SimpleTokenSessionLogin login = new SimpleTokenSessionLogin(id, config);
        login.exec();

        // 踢出本次登录后该用户过期的 token
        try {
            List<String> expiredTokens = login.getExpiredTokens();
            new SimpleTokenSessionKickout().execByToken(expiredTokens);
        } catch (Exception e) {
            log.warn("kick out tokens error after login", e);
        }
        return login.getSession().getToken();
    }

    /**
     * 根据账号密码进行会话登录
     * 账号名、账号密码错误会抛异常
     *
     * @param accountName 账号名
     * @param password    账号密码
     * @return token
     */
    public static String loginByAccount(String accountName, String password) {
        return loginByAccount("default", accountName, password, new LoginSetting());
    }

    /**
     * 根据账号密码进行会话登录
     * 账号名、账号密码错误会抛异常
     *
     * @param accountType 账号类型
     * @param accountName 账号名
     * @param password    账号密码
     * @return token
     */
    public static String loginByAccount(String accountType, String accountName, String password) {
        return loginByAccount(accountType, accountName, password, new LoginSetting());
    }

    /**
     * 根据账号密码进行会话登录，并指定所有登录参数
     * 账号名、账号密码错误会抛异常
     *
     * @param accountType 账号类型
     * @param accountName 账号名
     * @param password    账号密码
     * @param config      登录的参数
     * @return token
     */
    public static String loginByAccount(String accountType, String accountName, String password, LoginSetting config) {
        UserAccount account = UserManager.findAccountByName(accountType, accountName);
        UserManager.validatePassword(accountType, password, account.getPassword());
        account.setPassword(null);
        return login(account.getUserId(), config);
    }

    /**
     * 登出（注销当前上下文中对应的登录信息）
     */
    public static void logout() {
        // 构建一个登出领域对象执行登出逻辑
        new SimpleTokenSessionLogout().exec();
    }

    /**
     * 登出（注销用户的所有登录信息）
     *
     * @param id 用户 id
     */
    public static void logout(Long id) {
        if (id == null) {
            throw new SimpleTokenException("Login id can not be null");
        }
        logout(id.toString());
    }

    /**
     * 登出（注销用户的所有登录信息）
     *
     * @param id 用户 id
     */
    public static void logout(String id) {
        // 构建一个登出领域对象执行登出逻辑
        new SimpleTokenSessionLogout(id).exec();
    }

    /**
     * 登出（注销用户在某个客户端的所有登录信息）
     *
     * @param id     用户 id
     * @param client 客户端
     */
    public static void logout(Long id, String client) {
        if (id == null) {
            throw new SimpleTokenException("Login id can not be null");
        }
        logout(id.toString(), client);
    }

    /**
     * 登出（注销用户在某个客户端的所有登录信息）
     *
     * @param id     用户 id
     * @param client 客户端
     */
    public static void logout(String id, String client) {
        // 构建一个登出领域对象执行登出逻辑
        new SimpleTokenSessionLogout(id, client).exec();
    }

    /**
     * 踢用户下线
     *
     * @param id 用户 id
     */
    public static void kickout(Long id) {
        if (id == null) {
            throw new SimpleTokenException("Login id can not be null");
        }
        kickout(id.toString());
    }

    /**
     * 踢用户下线
     *
     * @param id 用户 id
     */
    public static void kickout(String id) {
        new SimpleTokenSessionKickout(id).exec();
    }

    /**
     * 踢用户下线
     *
     * @param id     用户 id
     * @param client 客户端
     */
    public static void kickout(Long id, String client) {
        if (id == null) {
            throw new SimpleTokenException("Login id can not be null");
        }
        kickout(id.toString(), client);
    }

    /**
     * 踢用户下线
     *
     * @param id     用户 id
     * @param client 客户端
     */
    public static void kickout(String id, String client) {
        new SimpleTokenSessionKickout(id, client).exec();
    }

    /**
     * 根据 token 踢对应的用户下线
     *
     * @param token token
     */
    public static void kickoutByToken(String token) {
        new SimpleTokenSessionKickout().execByToken(Collections.singletonList(token));
    }

    /**
     * 获取当前用户所拥有的权限集合
     *
     * @return 权限集合
     */
    public static List<String> getPermissions() {
        return findPermission().getPermissions();
    }

    /**
     * 判断当前用户是否有指定权限
     *
     * @param permission 权限
     */
    public static boolean hasPermission(String permission) {
        return findPermission().hasPermission(permission);
    }

    /**
     * 校验当前用户是否有指定的所有权限，无权限抛异常 InvalidPermissionException
     *
     * @param permissions 权限
     */
    public static void checkHasPermission(String... permissions) {
        if (permissions == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (!perm.hasPermission(permissions)) {
            throw new InvalidPermissionException(perm.getLastMatchArg(), false);
        }
    }

    /**
     * 校验当前用户是否有指定的所有权限，无权限抛异常 InvalidPermissionException
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
     * 校验当前用户是否有指定的任一权限，无权限抛异常 InvalidPermissionException
     *
     * @param permissions 权限
     */
    public static void checkAnyPermission(String... permissions) {
        if (permissions == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (!perm.anyPermission(permissions)) {
            throw new InvalidPermissionException(perm.getLastMatchArg(), false);
        }
    }

    /**
     * 校验当前用户是否有指定的任一权限，无权限抛异常 InvalidPermissionException
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
     * 校验当前用户是否无指定的所有权限，有权限抛异常 InvalidPermissionException
     *
     * @param permissions 权限
     */
    public static void checkNotPermission(String... permissions) {
        if (permissions == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (perm.anyPermission(permissions)) {
            throw new InvalidPermissionException(perm.getLastMatchArg(), true);
        }
    }

    /**
     * 校验当前用户是否无指定的所有权限，有权限抛异常 InvalidPermissionException
     *
     * @param permissions 权限
     */
    public static void checkNotPermission(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        checkNotPermission(permissions.toArray(new String[0]));
    }

    /**
     * 获取当前用户所拥有的角色集合
     *
     * @return 角色集合
     */
    public static List<String> getRoles() {
        return findPermission().getRoles();
    }

    /**
     * 判断当前用户是否有指定角色
     *
     * @param role 角色
     */
    public static boolean hasRole(String role) {
        return findPermission().hasRole(role);
    }

    /**
     * 校验当前用户是否有指定的所有角色，无角色抛异常 InvalidRoleException
     *
     * @param roles 角色
     */
    public static void checkHasRole(String... roles) {
        if (roles == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (!perm.hasRole(roles)) {
            throw new InvalidRoleException(perm.getLastMatchArg(), false);
        }
    }

    /**
     * 校验当前用户是否有指定的所有角色，无角色抛异常 InvalidRoleException
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
     * 校验当前用户是否有指定的任一角色，无角色抛异常 InvalidRoleException
     *
     * @param roles 角色
     */
    public static void checkAnyRole(String... roles) {
        if (roles == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (!perm.anyRole(roles)) {
            throw new InvalidRoleException(perm.getLastMatchArg(), false);
        }
    }

    /**
     * 校验当前用户是否有指定的任一角色，无角色抛异常 InvalidRoleException
     *
     * @param roles 角色
     */
    public static void checkAnyRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        checkAnyRole(roles.toArray(new String[0]));
    }

    /**
     * 校验当前用户是否无指定的所有角色，有角色抛异常 InvalidRoleException
     *
     * @param roles 角色
     */
    public static void checkNotRole(String... roles) {
        if (roles == null) {
            return;
        }
        SimpleTokenPermission perm = findPermission();
        if (perm.anyRole(roles)) {
            throw new InvalidRoleException(perm.getLastMatchArg(), true);
        }
    }

    /**
     * 校验当前用户是否无指定的所有角色，有角色抛异常 InvalidRoleException
     *
     * @param roles 角色
     */
    public static void checkNotRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        checkAnyRole(roles.toArray(new String[0]));
    }

    public static void clearThreadCache() {
        THREAD_LOCAL_SESSION.remove();
        THREAD_LOCAL_PERMISSION.remove();
    }

    public static SimpleTokenPermission findPermission() {
        SimpleTokenPermission permission = THREAD_LOCAL_PERMISSION.get();
        if (permission == null) {
            permission = new SimpleTokenPermission();
            THREAD_LOCAL_PERMISSION.set(permission);
        }
        return permission;
    }

}
