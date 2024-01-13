package dev.simpleframework.token.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验权限
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface TokenCheckPermission {

    /**
     * 需要校验的权限
     */
    String[] value();

    /**
     * 校验模式
     */
    CheckMode mode() default CheckMode.ANY;

}
