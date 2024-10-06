package dev.simpleframework.token.user;

import lombok.Data;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class TokenUserInfo {

    private String id;
    private boolean enable = true;

    public TokenUserInfo() {
    }

    public TokenUserInfo(String id) {
        this.id = id;
    }

    public TokenUserInfo(String id, boolean enable) {
        this.id = id;
        this.enable = enable;
    }

}
