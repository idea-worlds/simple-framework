package dev.simpleframework.token.login;

import lombok.Data;

/**
 * 账号信息
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class UserAccount {

    private String id;
    private String account;
    private String password;

}
