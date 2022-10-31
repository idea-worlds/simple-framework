package dev.simpleframework.crud.annotation;

import dev.simpleframework.crud.core.ModelIdStrategy;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Alias for javax.persistence.Id
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Id {

    /**
     * 主键策略
     */
    ModelIdStrategy strategy() default ModelIdStrategy.SNOWFLAKE;

}
