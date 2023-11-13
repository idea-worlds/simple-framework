package dev.simpleframework.crud.annotation;

import dev.simpleframework.core.util.Strings;
import dev.simpleframework.crud.core.ConditionType;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
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
@Repeatable(Conditions.class)
public @interface Condition {

    /**
     * 类字段名
     */
    String field() default "";

    /**
     * 条件类型
     */
    ConditionType type() default ConditionType.equal;

    /**
     * 当条件值为 null 时的默认值
     * 解析方式：{@link Strings#cast(String, Class)}
     */
    String defaultValueIfNull() default "";

}
