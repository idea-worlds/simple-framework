package dev.simpleframework.token.login;

/**
 * 账号密码校验器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@FunctionalInterface
public interface AccountPasswordValidator {

    /**
     * 校验账号密码是否匹配
     *
     * @param paramPassword  要匹配的密码
     * @param storedPassword 存储的实际密码
     * @return 是否相等
     */
    boolean validate(String paramPassword, String storedPassword);

}
