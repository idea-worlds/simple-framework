package dev.simpleframework.token.user;

import lombok.Data;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class UserInfo {

    private String id;
    private boolean enable = true;

    public UserInfo() {
    }

    public UserInfo(String id) {
        this.id = id;
    }

    public UserInfo(String id, boolean enable) {
        this.id = id;
        this.enable = enable;
    }

}
