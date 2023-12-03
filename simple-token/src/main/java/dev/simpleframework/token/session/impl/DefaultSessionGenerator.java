package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.session.SessionGenerator;
import dev.simpleframework.token.session.SessionInfo;
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
    public SessionInfo generate(UserInfo user, long expiredTime) {
        SessionInfo session = new SessionInfo(user.getId(), expiredTime);
        session.setAttrs(this.generateAttrs(user, expiredTime));
        session.setToken(this.generateToken(session));
        return session;
    }

    protected Map<String, Object> generateAttrs(UserInfo user, long expiredTime) {
        return Collections.emptyMap();
    }

    protected String generateToken(SessionInfo session) {
        SimpleTokenLoginConfig config = SimpleTokens.getGlobalConfig().getLogin();
        String token;
        switch (config.getTokenStyle()) {
            case UUID32 -> token = UUID.randomUUID().toString().replace("-", "");
            case JWT -> token = DefaultJwtToken.of(config.getTokenJwtSecretKey(), session).getToken();
            default -> token = UUID.randomUUID().toString();
        }
        return token;
    }

}
