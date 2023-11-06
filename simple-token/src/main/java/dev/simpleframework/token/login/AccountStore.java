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
     * @param account 账号名
     * @return 用户信息
     */
    AccountInfo get(String account);

}
