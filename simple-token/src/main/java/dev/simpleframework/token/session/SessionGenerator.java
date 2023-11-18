package dev.simpleframework.token.session;

import dev.simpleframework.token.user.UserInfo;

/**
 * 会话值生成器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface SessionGenerator {

    /**
     * 生成一个新的会话值
     *
     * @param user        用户信息
     * @param expiredTime 过期时间
     * @return 会话值
     */
    SessionInfo generate(UserInfo user, long expiredTime);

}
