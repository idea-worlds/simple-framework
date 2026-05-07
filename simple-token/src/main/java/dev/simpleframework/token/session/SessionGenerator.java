package dev.simpleframework.token.session;

import dev.simpleframework.token.user.UserInfo;

import java.util.Map;

/**
 * 会话值生成器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface SessionGenerator {

    /**
     * 生成会话值对象的自定义属性
     *
     * @param user        用户信息
     * @param createTime  创建时间
     * @param expiredTime 过期时间
     * @return 自定义属性
     */
    Map<String, Object> generateAttrs(UserInfo user, long createTime, long expiredTime);

    /**
     * 生成 token
     *
     * @param user  用户信息
     * @param attrs 自定义属性
     * @return 会话值
     */
    String generateToken(UserInfo user, Map<String, Object> attrs);

}
