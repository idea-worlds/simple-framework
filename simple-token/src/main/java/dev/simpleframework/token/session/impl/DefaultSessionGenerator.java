package dev.simpleframework.token.session.impl;

import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.session.SessionGenerator;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.util.Strings;

/**
 * 默认的会话值生成器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class DefaultSessionGenerator implements SessionGenerator {

    @Override
    public SessionInfo generate(SessionInfo args) {
        this.setAttrs(args);
        this.setToken(args);
        return args;
    }

    protected void setAttrs(SessionInfo info) {
        // nothing
    }

    protected void setToken(SessionInfo info) {
        String accountType = info.getAccountType();
        SimpleTokenLoginConfig config = SimpleTokens.getGlobalConfig().findLoginConfig(accountType);
        String token;
        switch (config.getTokenStyle()) {
            case UUID32 -> token = Strings.uuid32();
            case JWT -> token = DefaultJwtToken.of(config.getTokenJwtSecretKey(), info).getToken();
            default -> token = Strings.uuid();
        }
        info.setToken(token);
    }

}
