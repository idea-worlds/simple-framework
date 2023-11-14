package dev.simpleframework.token.session;

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
     * 账号类型
     */
    private String accountType;
    /**
     * 登录 id
     */
    private String loginId;
    /**
     * 用户名
     */
    private String userName;
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
    /**
     * 属性集
     */
    private Map<String, Object> attrs;

    public SessionInfo() {
        this.userName = "";
        this.createTime = System.currentTimeMillis();
        this.attrs = new HashMap<>();
    }

    public SessionInfo(String accountType, String loginId, String userName, long expiredTime) {
        this.accountType = accountType;
        this.loginId = loginId;
        this.userName = userName;
        this.createTime = System.currentTimeMillis();
        this.expiredTime = expiredTime;
        this.attrs = new HashMap<>();
    }

    public SessionInfo addAttr(String key, Object value) {
        this.attrs.put(key, value);
        return this;
    }

}
