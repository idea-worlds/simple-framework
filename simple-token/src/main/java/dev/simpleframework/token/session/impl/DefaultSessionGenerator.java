package dev.simpleframework.token.session.impl;

import cn.hutool.jwt.JWT;
import dev.simpleframework.token.SimpleTokenLoginConfig;
import dev.simpleframework.token.SimpleTokens;
import dev.simpleframework.token.exception.SimpleTokenException;
import dev.simpleframework.token.session.SessionGenerator;
import dev.simpleframework.token.session.SimpleTokenSession;
import dev.simpleframework.util.Strings;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 默认的会话值生成器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class DefaultSessionGenerator implements SessionGenerator {

    @Override
    public SimpleTokenSession generate(SimpleTokenSession args) {
        this.setAttrs(args);
        this.setToken(args);
        return args;
    }

    protected void setAttrs(SimpleTokenSession info) {
        // nothing
    }

    protected void setToken(SimpleTokenSession info) {
        String accountType = info.getAccountType();
        SimpleTokenLoginConfig config = SimpleTokens.getGlobalConfig().findLoginConfig(accountType);
        String token;
        switch (config.getTokenStyle()) {
            case UUID32 -> token = Strings.uuid32();
            case JWT -> token = this.generateJwt(config, info);
            default -> token = Strings.uuid();
        }
        info.setToken(token);
    }

    /**
     * 基于 hutool-jwt 生成 jwt. 本项目未引入依赖，请自行添加
     */
    protected String generateJwt(SimpleTokenLoginConfig config, SimpleTokenSession info) {
        String secretKey = config.getTokenJwtSecretKey();
        if (secretKey == null || secretKey.isBlank()) {
            throw new SimpleTokenException("Secret key for token jwt can not be empty. " +
                    "Please set in SimpleTokenLoginConfig.tokenJwtSecretKey.");
        }
        JWT jwt = JWT.create()
                .setKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .setSubject("token")
                .setJWTId(info.getAccountType() + ":" + info.getLoginId())
                .setIssuedAt(new Date(info.getCreateTime()))
                .setExpiresAt(new Date(info.getExpiredTime()))
                .setCharset(StandardCharsets.UTF_8);
        info.getAttrs().forEach(jwt::setPayload);
        return jwt.sign();
    }

}
