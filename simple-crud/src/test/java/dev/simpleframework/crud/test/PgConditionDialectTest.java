package dev.simpleframework.crud.test;

import dev.simpleframework.crud.dialect.condition.PgConditionDialect;
import dev.simpleframework.crud.test.support.TestModelField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL 条件方言单元测试。
 * 验证 PG 专属语法生成：数组操作符（@>、<@、&&）和 JSON 操作符（@>、<@、??、??|、??&）。
 * 同时验证 XML 模式下操作符的 CDATA 包裹行为。
 */
public class PgConditionDialectTest {

    static PgConditionDialect dialect = PgConditionDialect.DEFAULT;

    // ========== 数组条件：验证 PG 操作符 + String[] 的 ::text[] 类型转换 ==========

    /**
     * 场景：String 数组字段使用 arrayContains 条件。
     * 验证点：生成 @> 操作符，并追加 ::text[] 类型转换使 PG 正确解析 String[]。
     * 为什么测：String[] 在 PG 中需要显式类型转换，非 String 数组不需要。
     */
    @Test
    public void testArrayContains_withStringComponent_shouldAppendTextArrayCast() {
        var field = new TestModelField("tags", "tags", String.class);
        String result = dialect.arrayContains(field, "#{val}", true);
        assertTrue(result.contains("@>"));
        assertTrue(result.contains("::text[]"));
    }

    /**
     * 场景：非 String 数组（如 Long[]）使用 arrayContains。
     * 验证点：生成 @> 但不追加 ::text[]——Long 类型 PG 可隐式推导。
     */
    @Test
    public void testArrayContains_withNonStringComponent_shouldNotAppendTextArrayCast() {
        var field = new TestModelField("ids", "ids", Long.class);
        String result = dialect.arrayContains(field, "#{val}", true);
        assertTrue(result.contains("@>"));
        assertEquals(-1, result.indexOf("::text[]"));
    }

    /**
     * 场景：String 数组的 arrayContainedBy。
     * 验证点：生成 <@ 操作符 + ::text[] 转换。
     */
    @Test
    public void testArrayContainedBy_withStringComponent_shouldAppendTextArrayCast() {
        var field = new TestModelField("tags", "tags", String.class);
        String result = dialect.arrayContainedBy(field, "#{val}", true);
        assertTrue(result.contains("<@"));
        assertTrue(result.contains("::text[]"));
    }

    /** 同上，非 String 数组不加 ::text[]。 */
    @Test
    public void testArrayContainedBy_withNonStringComponent_shouldNotAppendTextArrayCast() {
        var field = new TestModelField("ids", "ids", Long.class);
        String result = dialect.arrayContainedBy(field, "#{val}", true);
        assertTrue(result.contains("<@"));
        assertEquals(-1, result.indexOf("::text[]"));
    }

    /**
     * 场景：String 数组的 arrayOverlap（相交判断）。
     * 验证点：生成 && 操作符 + ::text[] 转换。
     */
    @Test
    public void testArrayOverlap_withStringComponent_shouldAppendTextArrayCast() {
        var field = new TestModelField("tags", "tags", String.class);
        String result = dialect.arrayOverlap(field, "#{val}", true);
        assertTrue(result.contains("&&"));
        assertTrue(result.contains("::text[]"));
    }

    /** 同上，非 String 数组不加 ::text[]。 */
    @Test
    public void testArrayOverlap_withNonStringComponent_shouldNotAppendTextArrayCast() {
        var field = new TestModelField("ids", "ids", Long.class);
        String result = dialect.arrayOverlap(field, "#{val}", true);
        assertTrue(result.contains("&&"));
        assertEquals(-1, result.indexOf("::text[]"));
    }

    // ========== JSON 条件：验证 PG jsonb 操作符 ==========

    /**
     * 场景：jsonb 列用 jsonContains 判断包含关系。
     * 验证点：生成 PG jsonb @> 操作符。MySQL 用 JSON_CONTAINS 函数，PG 用操作符。
     */
    @Test
    public void testJsonContains_shouldUseAtGtOperator() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonContains(field, "#{val}", true);
        assertTrue(result.contains("@>"));
    }

    /** jsonb 被包含：生成 <@ 操作符。 */
    @Test
    public void testJsonContainedBy_shouldUseLtAtOperator() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonContainedBy(field, "#{val}", true);
        assertTrue(result.contains("<@"));
    }

    /**
     * 场景：jsonb 键存在性检查。
     * 验证点：生成 ?? 操作符（PG 9.4+ jsonb 专属）。MySQL 用 JSON_CONTAINS_PATH 函数。
     */
    @Test
    public void testJsonExistKey_shouldUseDoubleQuestionMark() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonExistKey(field, "#{val}", true);
        assertTrue(result.contains("??"));
    }

    /** 键存在（任意）：生成 ??| 操作符。 */
    @Test
    public void testJsonExistKeyAny_shouldUseQuestionMarkPipe() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonExistKeyAny(field, "#{val}", true);
        assertTrue(result.contains("??|"));
    }

    /** 键存在（全部）：生成 ??& 操作符。 */
    @Test
    public void testJsonExistKeyAll_shouldUseQuestionMarkAmpersand() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonExistKeyAll(field, "#{val}", true);
        assertTrue(result.contains("??&"));
    }

    // ========== XML 模式：MyBatis XML 脚本中特殊字符转义 ==========

    /**
     * 场景：xml=true（MyBatis XML 脚本模式）。
     * 验证点：操作符被 <![CDATA[...]]> 包裹，防止 XML 解析错误。
     * 为什么测：PG 操作符含 @、<、>、&、| 等 XML 特殊字符，不包裹会导致 MyBatis 解析失败。
     */
    @Test
    public void testXmlMode_shouldWrapOperatorInCdata() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonContains(field, "#{val}", true);
        assertTrue(result.contains("<![CDATA["));
        assertTrue(result.contains("]]>"));
    }

    /**
     * 场景：xml=false（非 XML 模式）。
     * 验证点：不生成 CDATA 包裹，操作符直接拼接。
     */
    @Test
    public void testNonXmlMode_shouldNotWrapOperatorInCdata() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonContains(field, "#{val}", false);
        assertEquals(-1, result.indexOf("<![CDATA["));
    }

}
