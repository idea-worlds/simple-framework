package dev.simpleframework.token.config;

import lombok.Data;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SimpleTokenCookieConfig {

    /**
     * 作用域
     */
    private String domain;
    /**
     * 路径
     */
    private String path = "/";
    /**
     * 是否只在 https 协议下有效
     */
    private Boolean secure = Boolean.FALSE;
    /**
     * 是否禁止 js 操作 Cookie
     */
    private Boolean httpOnly = Boolean.FALSE;
    /**
     * 第三方限制级别
     * Strict: 完全禁止
     * Lax: 部分允许
     * None: 不限制
     */
    private String sameSite;

}
