package dev.simpleframework.crud.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 查询条件
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface Conditions {

    Condition[] value();

}
