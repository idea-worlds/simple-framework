package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.session.SessionGenerator;
import dev.simpleframework.token.user.UserInfo;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * 默认的会话值生成器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class DefaultSessionGenerator implements SessionGenerator {

    @Override
    public Map<String, Object> generateAttrs(UserInfo user, long createTime, long expiredTime) {
        return Collections.emptyMap();
    }

    @Override
    public String generateToken(UserInfo user, Map<String, Object> attrs) {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
