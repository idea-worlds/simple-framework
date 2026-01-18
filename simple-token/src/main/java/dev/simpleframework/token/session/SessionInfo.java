package dev.simpleframework.token.session;

import dev.simpleframework.token.user.UserInfo;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 会话值对象，数据在登录时生成，在过期或登出时注销
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SessionInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录的账号类型
     */
    private String loginType;
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
     * 自定义属性
     */
    private Map<String, Object> attrs;

    public SessionInfo() {
        this.createTime = System.currentTimeMillis();
        this.attrs = new HashMap<>();
    }

    public SessionInfo(String loginId, long createTime, long expiredTime) {
        this.loginId = loginId;
        this.createTime = createTime;
        this.expiredTime = expiredTime;
        this.attrs = new HashMap<>();
    }

    /**
     * 获取会话的原生存时间
     */
    public long ttlByCreate(TimeUnit unit) {
        long millis = expiredTime - createTime;
        return unit.convert(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取会话的剩余生存时间
     */
    public long ttlByNow(TimeUnit unit) {
        long millis = expiredTime - System.currentTimeMillis();
        return unit.convert(millis, TimeUnit.MILLISECONDS);
    }

}
