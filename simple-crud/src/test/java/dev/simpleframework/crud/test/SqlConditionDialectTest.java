package dev.simpleframework.crud.test;

import dev.simpleframework.crud.dialect.condition.SqlConditionDialect;
import dev.simpleframework.crud.test.support.TestModelField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SqlConditionDialect 标准 SQL 方法单元测试。
 * SqlConditionDialect 是抽象基类，通过 PgConditionDialect.DEFAULT 实例测试继承的 13 个标准方法。
 * 这些方法生成 ANSI SQL，所有数据库通用。
 */
public class SqlConditionDialectTest {

    /** 通过 PgConditionDialect 实例测试继承的标准方法（SqlConditionDialect 为 abstract） */
    static SqlConditionDialect dialect = dev.simpleframework.crud.dialect.condition.PgConditionDialect.DEFAULT;

    /** 场景：equal 条件。验证点：生成 "col = val" 标准 SQL。 */
    @Test
    public void testEqual_shouldUseEqualsOperator() {
        var field = new TestModelField("name", "name", String.class);
        String result = dialect.equal(field, "#{val}", false);
        assertTrue(result.contains("name = #{val}"));
    }

    /** 场景：notEqual。验证点："col != val"。 */
    @Test
    public void testNotEqual_shouldUseNotEqualsOperator() {
        var field = new TestModelField("name", "name", String.class);
        String result = dialect.notEqual(field, "#{val}", false);
        assertTrue(result.contains("name != #{val}"));
    }

    /**
     * 场景：likeAll 全模糊匹配。
     * 验证点：concat('%%', val, '%%') 包裹。使用 concat 函数而非 || 拼接（跨数据库兼容）。
     */
    @Test
    public void testLikeAll_shouldWrapWithPercents() {
        var field = new TestModelField("name", "name", String.class);
        String result = dialect.likeAll(field, "#{val}", false);
        assertTrue(result.contains("LIKE concat('%%',#{val},'%%')"));
    }

    /** likeLeft 左模糊：concat('%%', val)。 */
    @Test
    public void testLikeLeft_shouldPrependPercent() {
        var field = new TestModelField("name", "name", String.class);
        String result = dialect.likeLeft(field, "#{val}", false);
        assertTrue(result.contains("LIKE concat('%%',#{val})"));
    }

    /** likeRight 右模糊：concat(val, '%%')。 */
    @Test
    public void testLikeRight_shouldAppendPercent() {
        var field = new TestModelField("name", "name", String.class);
        String result = dialect.likeRight(field, "#{val}", false);
        assertTrue(result.contains("LIKE concat(#{val},'%%')"));
    }

    /**
     * 场景：比较操作符在 XML 模式下的 CDATA 包裹。
     * 验证点：>、>=、<、<= 被 <![CDATA[...]]> 包裹。
     * 为什么测：这些字符在 MyBatis XML 中是特殊字符，不包裹会导致 XML 解析失败。
     */
    @Test
    public void testGreaterThan_xmlMode_shouldWrapInCdata() {
        var field = new TestModelField("age", "age", Integer.class);
        String result = dialect.greaterThan(field, "#{val}", true);
        assertTrue(result.contains("<![CDATA[ > ]]>"));
    }

    @Test
    public void testGreatEqual_xmlMode_shouldWrapInCdata() {
        var field = new TestModelField("age", "age", Integer.class);
        String result = dialect.greatEqual(field, "#{val}", true);
        assertTrue(result.contains("<![CDATA[ >= ]]>"));
    }

    @Test
    public void testLessThan_xmlMode_shouldWrapInCdata() {
        var field = new TestModelField("age", "age", Integer.class);
        String result = dialect.lessThan(field, "#{val}", true);
        assertTrue(result.contains("<![CDATA[ < ]]>"));
    }

    @Test
    public void testLessEqual_xmlMode_shouldWrapInCdata() {
        var field = new TestModelField("age", "age", Integer.class);
        String result = dialect.lessEqual(field, "#{val}", true);
        assertTrue(result.contains("<![CDATA[ <= ]]>"));
    }

    /** 场景：IN 条件。验证点："col IN (values)"。 */
    @Test
    public void testIn_shouldUseInSyntax() {
        var field = new TestModelField("id", "id", Long.class);
        String result = dialect.in(field, "(1,2,3)", false);
        assertTrue(result.contains("IN (1,2,3)"));
    }

    /** NOT IN 条件："col NOT IN (values)"。 */
    @Test
    public void testNotIn_shouldUseNotInSyntax() {
        var field = new TestModelField("id", "id", Long.class);
        String result = dialect.notIn(field, "(1,2,3)", false);
        assertTrue(result.contains("NOT IN (1,2,3)"));
    }

    /**
     * 场景：IS NULL 条件。
     * 验证点：不绑定参数值，直接 "col IS NULL"。value 参数被忽略。
     * 为什么测：NULL 检查不需要参数绑定，直接写在 SQL 中。
     */
    @Test
    public void testIsNull_shouldIgnoreValueParam() {
        var field = new TestModelField("name", "name", String.class);
        String result = dialect.isNull(field, "#{val}", false);
        assertTrue(result.contains("IS NULL"));
    }

    /** IS NOT NULL 条件："col IS NOT NULL"，不绑定值。 */
    @Test
    public void testNotNull_shouldIgnoreValueParam() {
        var field = new TestModelField("name", "name", String.class);
        String result = dialect.notNull(field, "#{val}", false);
        assertTrue(result.contains("IS NOT NULL"));
    }

}
