package dev.simpleframework.token.user;

import lombok.Data;

/**
 * 账号信息
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class UserAccount {

    /**
     * 用户 id
     */
    private String userId;
    /**
     * 账号名
     */
    private String name;
    /**
     * 密码
     */
    private String password;

}
