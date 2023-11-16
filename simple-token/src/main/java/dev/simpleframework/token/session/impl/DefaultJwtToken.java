package dev.simpleframework.token.session.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.RegisteredPayload;
import dev.simpleframework.token.exception.SimpleTokenException;
import dev.simpleframework.token.session.SessionInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * 基于 hutool-jwt 生成 jwt. 本项目未引入依赖，请自行添加
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultJwtToken {

    private String secretKey;
    private String token;
    private SessionInfo session;

    public static DefaultJwtToken of(String secretKey, String token) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new SimpleTokenException("Secret key for token jwt can not be empty. " +
                    "Please set in SimpleTokenLoginConfig.tokenJwtSecretKey.");
        }
        return new DefaultJwtToken(secretKey, token)
                .setSession();
    }

    public static DefaultJwtToken of(String secretKey, SessionInfo session) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new SimpleTokenException("Secret key for token jwt can not be empty. " +
                    "Please set in SimpleTokenLoginConfig.tokenJwtSecretKey.");
        }
        return new DefaultJwtToken(secretKey, session)
                .setToken();
    }

    private DefaultJwtToken setToken() {
        JWT jwt = JWT.create()
                .setKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .setSubject("token")
                .setJWTId(this.session.getLoginId())
                .setIssuer(this.session.getUserName())
                .setIssuedAt(new Date(this.session.getCreateTime()))
                .setExpiresAt(new Date(this.session.getExpiredTime()))
                .setCharset(StandardCharsets.UTF_8);
        this.session.getAttrs().forEach(jwt::setPayload);
        this.token = jwt.sign();
        return this;
    }

    private DefaultJwtToken setSession() {
        JWT jwt = JWT.of(this.token);
        if (jwt.validate(0)) {
            SessionInfo session = new SessionInfo();
            session.setToken(this.token);
            JSONObject payload = jwt.getPayload().getClaimsJson();
            session.setLoginId(payload.getStr(RegisteredPayload.JWT_ID));
            session.setUserName(payload.getStr(RegisteredPayload.ISSUER));
            session.setCreateTime(payload.getDate(RegisteredPayload.ISSUED_AT).getTime());
            session.setExpiredTime(payload.getDate(RegisteredPayload.EXPIRES_AT).getTime());
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                String key = entry.getKey();
                if (RegisteredPayload.JWT_ID.equals(key)
                        || RegisteredPayload.ISSUER.equals(key)
                        || RegisteredPayload.ISSUED_AT.equals(key)
                        || RegisteredPayload.EXPIRES_AT.equals(key)) {
                    continue;
                }
                session.addAttr(key, entry.getValue());
            }
            this.session = session;
        }
        return this;
    }

    private DefaultJwtToken(String secretKey, String token) {
        this.secretKey = secretKey;
        this.token = token;
    }

    private DefaultJwtToken(String secretKey, SessionInfo session) {
        this.secretKey = secretKey;
        this.session = session;
    }

}
