package dev.simpleframework.crud.core;

import dev.simpleframework.util.Snowflake;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public enum ModelIdStrategy {
    /**
     * 雪花算法
     *
     * @see Snowflake
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
