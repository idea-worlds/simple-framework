package dev.simpleframework.crud.test;

import dev.simpleframework.crud.dialect.Dialects;
import dev.simpleframework.crud.dialect.condition.H2ConditionDialect;
import dev.simpleframework.crud.dialect.condition.MySqlConditionDialect;
import dev.simpleframework.crud.dialect.condition.PgConditionDialect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Dialects 方言注册与自动检测单元测试。
 * 验证三种内置方言的 DEFAULT 实例存在，以及手动注册 API 正常工作。
 * JDBC URL 自动匹配由 DialectAutoDetectionTest（集成测试）覆盖。
 */
public class DialectsTest {

    /**
     * 场景：验证 PG 方言已预注册。
     * 验证点：PgConditionDialect.DEFAULT 不为 null。
     * 为什么测：Dialects 静态块中预注册了 postgresql 方言，DEFAULT 是默认回退选项。
     */
    @Test
    public void testPgConditionDialect_shouldHaveDefaultInstance() {
        assertNotNull(PgConditionDialect.DEFAULT);
    }

    /** 验证 MySQL 方言已预注册。 */
    @Test
    public void testMysqlConditionDialect_shouldHaveDefaultInstance() {
        assertNotNull(MySqlConditionDialect.DEFAULT);
    }

    /** 验证 H2 方言已预注册。 */
    @Test
    public void testH2ConditionDialect_shouldHaveDefaultInstance() {
        assertNotNull(H2ConditionDialect.DEFAULT);
    }

    /**
     * 场景：用户手动注册自定义方言（如 Oracle、SQL Server 等）。
     * 验证点：registerConditionDialect() 不抛异常。
     * 为什么测：框架内置 PG/MySQL/H2，其他数据库需用户手动调用此 API。
     */
    @Test
    public void testRegisterCustomDialect_shouldNotThrow() {
        var custom = new PgConditionDialect() {};
        Dialects.registerConditionDialect("testdb", custom);
    }

}
