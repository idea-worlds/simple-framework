package dev.simpleframework.token.constant;

/**
 * 登录设备超出最大数量后的策略
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public enum LoginMaxStrategy {

    /**
     * 踢出最先登录的设备
     */
    KICK_OUT_FIRST_CREATE,
    /**
     * 踢出最先过期的设备
     */
    KICK_OUT_FIRST_EXPIRE,
    /**
     * 踢出所有设备
     */
    KICK_OUT_ALL,
    /**
     * 拒绝登录
     */
    REJECT;
}
