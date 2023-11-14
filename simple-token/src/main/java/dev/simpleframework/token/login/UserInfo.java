package dev.simpleframework.token.login;

import dev.simpleframework.token.constant.UserStatus;
import lombok.Data;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class UserInfo {

    private String id;
    private String name;
    private UserStatus status;

    public UserInfo() {
    }

    public UserInfo(String id) {
        this.id = id;
        this.status = UserStatus.NORMAL;
    }

    public UserInfo(String id, UserStatus status) {
        this.id = id;
        this.name = "";
        this.status = status;
    }

    public UserInfo(String id, String name, UserStatus status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

}
