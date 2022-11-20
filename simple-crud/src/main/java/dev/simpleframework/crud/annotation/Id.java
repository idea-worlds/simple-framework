package dev.simpleframework.crud.annotation;

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
    Type type() default Type.SNOWFLAKE;

    enum Type {
        /**
         * 雪花算法
         */
        SNOWFLAKE,
        /**
         * UUID.replace("-", "")
         */
        UUID32,
        /**
         * UUID
         */
        UUID36,
        /**
         * 数据库自增主键（数据库表需要定义主键自增）
         */
        AUTO_INCREMENT
    }

}
