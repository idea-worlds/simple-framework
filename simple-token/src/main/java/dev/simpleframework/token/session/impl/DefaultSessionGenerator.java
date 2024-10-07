package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.constant.TokenStyle;
import dev.simpleframework.token.session.SessionGenerator;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.user.TokenUserInfo;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 默认的会话值生成器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class DefaultSessionGenerator implements SessionGenerator {

    @Override
    public SessionInfo generate(TokenUserInfo user, long expiredTime) {
        SessionInfo session = new SessionInfo(user.getId(), expiredTime);
        session.changeAttrs(user, expiredTime);
        session.setToken(this.generateToken(session));
        return session;
    }

    protected String generateToken(SessionInfo session) {
        SimpleTokenLoginConfig config = SimpleTokens.getGlobalConfig().getLogin();
        String token;
        if (Objects.requireNonNull(config.getTokenStyle()) == TokenStyle.UUID32) {
            token = UUID.randomUUID().toString().replace("-", "");
        } else {
            token = UUID.randomUUID().toString();
        }
        return token;
    }

}
