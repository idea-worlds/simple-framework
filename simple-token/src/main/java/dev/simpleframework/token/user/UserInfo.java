package dev.simpleframework.token.user;

import dev.simpleframework.token.constant.UserStatus;
import lombok.Data;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class UserInfo {

    private String id;
    private UserStatus status;

    public UserInfo() {
    }

    public UserInfo(String id) {
        this.id = id;
        this.status = UserStatus.ENABLE;
    }

    public UserInfo(String id, UserStatus status) {
        this.id = id;
        this.status = status;
    }

}
