package dev.simpleframework.token.constant;

/**
 * 用户状态
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public enum UserStatus {

    /**
     * 未启用（未审核）
     */
    INACTIVE,
    /**
     * 正常
     */
    ENABLE,
    /**
     * 禁用（停用）
     */
    DISABLED,
    /**
     * 锁定
     */
    LOCKED

}
