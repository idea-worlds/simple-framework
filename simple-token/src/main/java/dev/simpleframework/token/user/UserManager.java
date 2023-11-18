package dev.simpleframework.token.user;

import dev.simpleframework.token.constant.UserStatus;
import dev.simpleframework.token.exception.*;

/**
 * 用户管理器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public final class UserManager {

    private static UserQuery QUERY = UserQuery.DEFAULT;
    private static UserAccountPasswordValidator VALIDATOR = UserAccountPasswordValidator.DEFAULT;

    private UserManager() {
    }

    /**
     * 注册用户信息查询器
     *
     * @param query 查询器
     */
    public synchronized static void registerQuery(UserQuery query) {
        QUERY = query;
    }

    /**
     * 注册账号密码校验器
     *
     * @param validator 账号密码校验器
     */
    public synchronized static void registerPasswordValidator(UserAccountPasswordValidator validator) {
        VALIDATOR = validator;
    }

    /**
     * 获取用户信息，查无用户时抛异常，状态异常时抛异常
     *
     * @param loginId 登录 id
     * @return 用户信息
     */
    public static UserInfo findInfoById(String loginId) {
        validQuery();
        UserInfo info = QUERY.getInfoById(loginId);
        if (info == null) {
            throw new LoginUserNotFoundException("Login user can not be found");
        }
        UserStatus status = info.getStatus();
        if (status == UserStatus.INACTIVE) {
            throw new LoginUserInactiveException("Login user inactive");
        } else if (status == UserStatus.DISABLED) {
            throw new LoginUserDisabledException("Login user disabled");
        } else if (status == UserStatus.LOCKED) {
            throw new LoginUserLockedException("Login user locked");
        }
        return info;
    }

    /**
     * 获取账号信息，查无账号时抛异常
     *
     * @param accountType 账号类型
     * @param accountName 账号名
     * @return 账号信息
     */
    public static UserAccount findAccountByName(String accountType, String accountName) {
        validQuery();
        UserAccount account = QUERY.getAccountByName(accountType, accountName);
        if (account == null) {
            throw new LoginAccountNotFoundException("Login account can not be found");
        }
        return account;
    }

    /**
     * 校验账号密码，不匹配时抛异常
     *
     * @param accountType    账号类型
     * @param paramPassword  要匹配的密码
     * @param storedPassword 存储的实际密码
     */
    public static void validatePassword(String accountType, String paramPassword, String storedPassword) {
        if (paramPassword == null && storedPassword == null) {
            return;
        }
        boolean match = VALIDATOR.validate(accountType, paramPassword, storedPassword);
        if (!match) {
            throw new LoginPasswordInvalidException("Invalid account password");
        }
    }

    private static void validQuery() {
        if (QUERY == null) {
            throw new ImplementationNotFoundException(UserQuery.class, UserManager.class);
        }
    }

}
