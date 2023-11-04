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
public class SimpleTokenSession implements Serializable {
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

    public SimpleTokenSession() {
        this.createTime = System.currentTimeMillis();
        this.attrs = new HashMap<>();
    }

    public SimpleTokenSession(String accountType, String loginId, long expiredTime) {
        this.accountType = accountType;
        this.loginId = loginId;
        this.createTime = System.currentTimeMillis();
        this.expiredTime = expiredTime;
        this.attrs = new HashMap<>();
    }

    public SimpleTokenSession addAttr(String key, Object value) {
        this.attrs.put(key, value);
        return this;
    }

}
