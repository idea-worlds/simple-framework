package dev.simpleframework.token.login;

import dev.simpleframework.token.exception.ImplementationNotFoundException;
import dev.simpleframework.token.exception.LoginAccountNotFoundException;
import dev.simpleframework.token.exception.LoginInvalidPasswordException;

import java.util.HashMap;
import java.util.Map;

/**
 * 账号密码管理器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public final class AccountManager {

    private static final Map<String, AccountStore> STORE = new HashMap<>();
    private static final Map<String, AccountPasswordValidator> VALIDATORS = new HashMap<>();

    private static final AccountPasswordValidator DEFAULT_VALIDATOR = (param, store) -> param != null && param.equals(store);

    private AccountManager() {

    }

    /**
     * 注册账号信息存储器，用于获取账号信息
     *
     * @param accountType 账号类型
     * @param store       账号信息存储器
     */
    public synchronized static void registerStore(String accountType, AccountStore store) {
        STORE.put(accountType, store);
    }

    /**
     * 注册账号密码校验器，用于获取账号密码
     *
     * @param accountType 账号类型
     * @param validator   账号密码校验器
     */
    public synchronized static void registerValidator(String accountType, AccountPasswordValidator validator) {
        VALIDATORS.put(accountType, validator);
    }

    /**
     * 获取账号信息，查无账号时抛异常
     *
     * @param accountType 账号类型
     * @param accountName 账号名
     * @return 账号信息
     */
    public static AccountInfo findInfo(String accountType, String accountName) {
        AccountStore store = STORE.get(accountType);
        if (store == null) {
            throw new ImplementationNotFoundException(AccountStore.class, AccountManager.class);
        }
        AccountInfo info = store.get(accountName);
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
        AccountPasswordValidator validator = VALIDATORS.getOrDefault(accountType, DEFAULT_VALIDATOR);
        boolean match = validator.validate(paramPassword, storedPassword);
        if (!match) {
            throw new LoginInvalidPasswordException("Invalid account password");
        }
    }

}
