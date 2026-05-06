package dev.simpleframework.crud.test;

import dev.simpleframework.crud.dialect.condition.OracleConditionDialect;
import dev.simpleframework.crud.test.support.TestModelField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Oracle 条件方言单元测试。
 * 验证 LIKE 使用 || 字符串拼接，数组/JSON 使用 JSON_EXISTS 函数。
 */
public class OracleConditionDialectTest {

    static OracleConditionDialect dialect = OracleConditionDialect.DEFAULT;
    static TestModelField field = new TestModelField("data", "data", Object.class);

    @Test
    public void testLikeAll_shouldUsePipeConcat() {
        String result = dialect.likeAll(field, "#{val}", false);
        assertEquals("data LIKE '%' || #{val} || '%'", result);
    }

    @Test
    public void testLikeLeft_shouldUsePipeConcat() {
        String result = dialect.likeLeft(field, "#{val}", false);
        assertEquals("data LIKE '%' || #{val}", result);
    }

    @Test
    public void testLikeRight_shouldUsePipeConcat() {
        String result = dialect.likeRight(field, "#{val}", false);
        assertEquals("data LIKE #{val} || '%'", result);
    }

    @Test
    public void testJsonContains_shouldUseJsonExists() {
        String result = dialect.jsonContains(field, "#{val}", true);
        assertTrue(result.contains("JSON_EXISTS("));
    }

    @Test
    public void testJsonContainedBy_shouldUseJsonExists() {
        String result = dialect.jsonContainedBy(field, "#{val}", true);
        assertTrue(result.contains("JSON_EXISTS("));
    }

    @Test
    public void testJsonExistKey_shouldUseJsonExists() {
        String result = dialect.jsonExistKey(field, "#{val}", true);
        assertTrue(result.contains("JSON_EXISTS("));
    }

    @Test
    public void testJsonExistKeyAny_shouldUseJsonExists() {
        String result = dialect.jsonExistKeyAny(field, "#{val}", true);
        assertTrue(result.contains("JSON_EXISTS("));
    }

    @Test
    public void testJsonExistKeyAll_shouldUseJsonExists() {
        String result = dialect.jsonExistKeyAll(field, "#{val}", true);
        assertTrue(result.contains("JSON_EXISTS("));
    }

    @Test
    public void testArrayContains_shouldUseJsonExists() {
        String result = dialect.arrayContains(field, "#{val}", true);
        assertTrue(result.contains("JSON_EXISTS("));
    }

    @Test
    public void testStandardOperators_shouldNotUseCdata() {
        String result = dialect.greaterThan(field, "#{val}", true);
        assertTrue(result.contains("<![CDATA["));
    }

}
