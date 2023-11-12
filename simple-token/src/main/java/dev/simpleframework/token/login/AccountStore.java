package dev.simpleframework.token.login;

/**
 * 账号信息存储器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface AccountStore {

    /**
     * 根据账号名获取用户信息
     *
     * @param accountType 账号类型
     * @param accountName 账号名
     * @return 用户信息
     */
    AccountInfo getInfoByName(String accountType, String accountName);

    AccountStore DEFAULT = (accountType, accountName) -> null;

}
