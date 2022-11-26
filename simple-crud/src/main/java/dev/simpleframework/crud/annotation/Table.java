package dev.simpleframework.crud.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {

    /**
     * 表名
     */
    String name() default "";

    /**
     * 表空间
     */
    String schema() default "";

}
