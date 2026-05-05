package dev.simpleframework.crud.test;

import dev.simpleframework.crud.dialect.condition.MySqlConditionDialect;
import dev.simpleframework.crud.test.support.TestModelField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MySQL 条件方言单元测试。
 * 验证 MySQL 专属语法生成：数组和 JSON 条件映射为 JSON_CONTAINS / JSON_CONTAINS_PATH / JSON_OVERLAPS 函数。
 * MySQL 没有原生数组类型和 PG 风格操作符，全部通过 JSON 函数实现。
 */
public class MySqlConditionDialectTest {

    static MySqlConditionDialect dialect = MySqlConditionDialect.DEFAULT;

    // ========== 数组条件 → JSON 函数映射 ==========

    /**
     * 场景：MySQL 中数组条件（存为 JSON 列）。
     * 验证点：arrayContains → JSON_CONTAINS(col, val)。
     * 为什么测：MySQL 无原生数组，用 JSON 列替代。PG 用 @> 操作符，MySQL 用 JSON_CONTAINS 函数。
     */
    @Test
    public void testArrayContains_shouldUseJsonContains() {
        var field = new TestModelField("tags", "tags", Object.class);
        String result = dialect.arrayContains(field, "#{val}", true);
        assertTrue(result.contains("JSON_CONTAINS(tags, #{val})"));
    }

    /**
     * 场景：arrayContainedBy（数组被包含）。
     * 验证点：参数顺序反转——JSON_CONTAINS(val, col)。
     * 为什么测：被包含 = 反向包含，MySQL 通过交换参数实现。
     */
    @Test
    public void testArrayContainedBy_shouldSwapArgs() {
        var field = new TestModelField("tags", "tags", Object.class);
        String result = dialect.arrayContainedBy(field, "#{val}", true);
        assertTrue(result.contains("JSON_CONTAINS(#{val}, tags)"));
    }

    /**
     * 场景：arrayOverlap（数组相交判断）。
     * 验证点：使用 MySQL 8.0.17+ 的 JSON_OVERLAPS 函数。
     */
    @Test
    public void testArrayOverlap_shouldUseJsonOverlaps() {
        var field = new TestModelField("tags", "tags", Object.class);
        String result = dialect.arrayOverlap(field, "#{val}", true);
        assertTrue(result.contains("JSON_OVERLAPS(tags, #{val})"));
    }

    // ========== JSON 条件 ==========

    /**
     * 场景：jsonContains（JSON 包含）。
     * 验证点：JSON_CONTAINS(col, val)。与 arrayContains 相同——MySQL 中数组和 JSON 统一用此函数。
     */
    @Test
    public void testJsonContains_shouldUseJsonContains() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonContains(field, "#{val}", true);
        assertTrue(result.contains("JSON_CONTAINS(data, #{val})"));
    }

    /** jsonContainedBy：参数反转——JSON_CONTAINS(val, col)。 */
    @Test
    public void testJsonContainedBy_shouldSwapArgs() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonContainedBy(field, "#{val}", true);
        assertTrue(result.contains("JSON_CONTAINS(#{val}, data)"));
    }

    /**
     * 场景：jsonExistKey（键存在性检查）。
     * 验证点：JSON_CONTAINS_PATH(col, 'one', val)——'one' 表示任意一个键存在即满足。
     * 为什么测：PG 用 ?? 操作符，MySQL 用 JSON_CONTAINS_PATH 函数，语法完全不同。
     */
    @Test
    public void testJsonExistKey_shouldUseJsonContainsPathOne() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonExistKey(field, "#{val}", true);
        assertTrue(result.contains("JSON_CONTAINS_PATH(data, 'one', #{val})"));
    }

    /** jsonExistKeyAny：同单键，'one' 语义。 */
    @Test
    public void testJsonExistKeyAny_shouldUseJsonContainsPathOne() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonExistKeyAny(field, "#{val}", true);
        assertTrue(result.contains("JSON_CONTAINS_PATH(data, 'one', #{val})"));
    }

    /**
     * 场景：jsonExistKeyAll（全部键存在）。
     * 验证点：JSON_CONTAINS_PATH(col, 'all', val)——'all' 表示全部键必须存在。
     */
    @Test
    public void testJsonExistKeyAll_shouldUseJsonContainsPathAll() {
        var field = new TestModelField("data", "data", Object.class);
        String result = dialect.jsonExistKeyAll(field, "#{val}", true);
        assertTrue(result.contains("JSON_CONTAINS_PATH(data, 'all', #{val})"));
    }

}
