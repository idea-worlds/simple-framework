package dev.simpleframework.crud.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Alias for javax.persistence.Column
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Column {

    /**
     * 表字段名
     */
    String name() default "";

    /**
     * 是否可新增
     */
    boolean insertable() default true;

    /**
     * 是否可更新
     */
    boolean updatable() default true;

    /**
     * 是否可查询
     */
    boolean selectable() default true;

}
