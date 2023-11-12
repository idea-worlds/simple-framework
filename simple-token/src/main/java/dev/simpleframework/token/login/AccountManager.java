package dev.simpleframework.token.login;

import dev.simpleframework.token.exception.ImplementationNotFoundException;
import dev.simpleframework.token.exception.LoginAccountNotFoundException;
import dev.simpleframework.token.exception.LoginInvalidPasswordException;

/**
 * 账号密码管理器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public final class AccountManager {

    private static AccountStore STORE = AccountStore.DEFAULT;
    private static AccountPasswordValidator VALIDATOR = AccountPasswordValidator.DEFAULT;

    private AccountManager() {
    }

    /**
     * 注册账号信息存储器，用于获取账号信息
     *
     * @param store 账号信息存储器
     */
    public synchronized static void registerStore(AccountStore store) {
        STORE = store;
    }

    /**
     * 注册账号密码校验器，用于获取账号密码
     *
     * @param validator 账号密码校验器
     */
    public synchronized static void registerValidator(AccountPasswordValidator validator) {
        VALIDATOR = validator;
    }

    /**
     * 获取账号信息，查无账号时抛异常
     *
     * @param accountType 账号类型
     * @param accountName 账号名
     * @return 账号信息
     */
    public static AccountInfo findInfoByName(String accountType, String accountName) {
        validStore();
        AccountInfo info = STORE.getInfoByName(accountType, accountName);
        if (info == null) {
            throw new LoginAccountNotFoundException("Login account can not be found");
        }
        return info;
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
            throw new LoginInvalidPasswordException("Invalid account password");
        }
    }

    private static void validStore() {
        if (STORE == null) {
            throw new ImplementationNotFoundException(AccountStore.class, AccountManager.class);
        }
    }

}
