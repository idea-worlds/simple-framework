package dev.simpleframework.token;

import dev.simpleframework.token.exception.SimpleTokenException;
import dev.simpleframework.token.login.*;
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

    private static final SimpleTokenConfig CONFIG = null;

    private SimpleTokens() {
    }

    public static SimpleTokenConfig getGlobalConfig() {
        return CONFIG;
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
        SimpleTokenLogin login = new SimpleTokenLogin(id, config);
        login.exec();

        // 踢出本次登录后改账号过期的 token
        try {
            List<String> expiredTokens = login.getExpiredTokens();
            new SimpleTokenKickout().execByToken(expiredTokens);
        } catch (Exception e) {
            log.warn("kick out tokens error after login", e);
        }
        return login.getToken();
    }

    /**
     * 根据账号密码进行会话登录
     * 账号名、账号密码错误会抛异常
     *
     * @param account  账号名
     * @param password 账号密码
     * @return token
     */
    public static String loginByAccount(String account, String password) {
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
    public static String loginByAccount(String account, String password, LoginSetting config) {
        String accountType = config.getAccountType();
        AccountInfo info = AccountManager.findInfo(accountType, account);
        AccountManager.validatePassword(accountType, password, info.getPassword());
        return login(info.getId(), config);
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

}
