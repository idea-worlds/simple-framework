package dev.simpleframework.token.session;

import dev.simpleframework.token.user.TokenUserInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 会话值对象，数据在登录时生成，在过期或登出时注销
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SessionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 登录 id
     */
    private String loginId;
    /**
     * 创建时间
     */
    private long createTime;
    /**
     * 过期时间
     */
    private long expiredTime;
    /**
     * token
     */
    private String token;

    public SessionInfo() {
        this.createTime = System.currentTimeMillis();
    }

    public SessionInfo(String loginId, long expiredTime) {
        this.loginId = loginId;
        this.createTime = System.currentTimeMillis();
        this.expiredTime = expiredTime;
    }

    public void changeAttrs(SessionInfo info) {
        // 子类需实现自定义属性
    }

    public void changeAttrs(TokenUserInfo user, long expiredTime) {
        // 子类需实现自定义属性
    }

}
