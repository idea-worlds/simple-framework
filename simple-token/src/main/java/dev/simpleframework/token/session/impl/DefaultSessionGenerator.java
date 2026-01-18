package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.session.SessionGenerator;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.user.UserInfo;

import java.util.UUID;

/**
 * 默认的会话值生成器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class DefaultSessionGenerator implements SessionGenerator {

    @Override
    public SessionInfo generate(UserInfo user, long createTime, long expiredTime) {
        SessionInfo session = new SessionInfo(user.getId(), createTime, expiredTime);
        this.changeAttrs(session, user);
        session.setToken(UUID.randomUUID().toString().replace("-", ""));
        return session;
    }

    protected void changeAttrs(SessionInfo session, UserInfo user) {

    }

}
