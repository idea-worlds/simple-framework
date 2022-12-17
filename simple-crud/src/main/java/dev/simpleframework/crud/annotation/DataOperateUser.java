package dev.simpleframework.crud.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 数据操作用户
 *
 * @author loyayz (loyayz@foxmail.com)
 * @see dev.simpleframework.crud.helper.DataFillStrategy
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface DataOperateUser {
}
