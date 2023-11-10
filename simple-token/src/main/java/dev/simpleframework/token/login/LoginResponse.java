package dev.simpleframework.token.login;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public record LoginResponse(String token, long expiredTime) {
}
