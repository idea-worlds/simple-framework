package dev.simpleframework.crud.test;

import dev.simpleframework.crud.dialect.condition.H2ConditionDialect;
import dev.simpleframework.crud.test.support.TestModelField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * H2 条件方言单元测试。
 * H2 在 PostgreSQL 兼容模式下支持 @>、<@、&& 等操作符，但不需要 ::text[] 类型转换。
 * H2ConditionDialect 继承 PgConditionDialect，仅覆盖数组方法去除 ::text[]。
 * JSON 方法完全继承 PG 实现。
 */
public class H2ConditionDialectTest {

    static H2ConditionDialect dialect = H2ConditionDialect.DEFAULT;

    /**
     * 场景：H2(PG 模式) 的 String 数组 arrayContains。
     * 验证点：生成 @> 操作符但不追加 ::text[]。与 PgConditionDialect 的唯一差异。
     * 为什么测：H2 不需要 ::text[] 显式类型转换，PG 需要。集成测试用 H2，此差异必须正确。
     */
    @Test
    public void testArrayContains_shouldNotAppendTextArrayCast() {
        var field = new TestModelField("tags", "tags", String.class);
        String result = dialect.arrayContains(field, "#{val}", true);
        assertTrue(result.contains("@>"));
        assertEquals(-1, result.indexOf("::text[]"));
    }

    /** H2 的 arrayContainedBy：生成 <@，无 ::text[]。 */
    @Test
    public void testArrayContainedBy_shouldNotAppendTextArrayCast() {
        var field = new TestModelField("tags", "tags", String.class);
        String result = dialect.arrayContainedBy(field, "#{val}", true);
        assertTrue(result.contains("<@"));
        assertEquals(-1, result.indexOf("::text[]"));
    }

    /** H2 的 arrayOverlap：生成 &&，无 ::text[]。 */
    @Test
    public void testArrayOverlap_shouldNotAppendTextArrayCast() {
        var field = new TestModelField("tags", "tags", String.class);
        String result = dialect.arrayOverlap(field, "#{val}", true);
        assertTrue(result.contains("&&"));
        assertEquals(-1, result.indexOf("::text[]"));
    }

    /**
     * 场景：JSON 方法继承自 PgConditionDialect。
     * 验证点：jsonContains 仍生成 @> 操作符，与 PG 一致。
     * 为什么测：H2 未覆盖 JSON 方法，验证继承链正确。
     */
    @Test
    public void testJsonContains_shouldInheritPgMethod() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonContains(field, "#{val}", true);
        assertTrue(result.contains("@>"));
    }

}
