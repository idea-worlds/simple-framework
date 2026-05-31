package dev.simpleframework.token.config;

import lombok.Data;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SimpleTokenCookieConfig {

    /**
     * 是否启用 cookie 写入
     */
    private Boolean enabled = Boolean.TRUE;
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
    private Boolean secure = Boolean.TRUE;
    /**
     * 是否禁止 js 操作 Cookie
     */
    private Boolean httpOnly = Boolean.TRUE;
    /**
     * 第三方限制级别
     * Strict: 完全禁止
     * Lax: 部分允许
     * None: 不限制
     */
    private String sameSite = "Lax";

}
