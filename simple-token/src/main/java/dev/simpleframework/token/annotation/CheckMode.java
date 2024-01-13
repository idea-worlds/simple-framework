package dev.simpleframework.token.annotation;

/**
 * 注解鉴权的校验模式
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public enum CheckMode {

    /**
     * 只需有其中一个元素
     */
    ANY,
    /**
     * 必须有所有的元素
     */
    ALL,
    /**
     * 没有指定的元素
     */
    NOT

}
