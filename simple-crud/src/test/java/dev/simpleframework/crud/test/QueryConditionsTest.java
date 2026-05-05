package dev.simpleframework.crud.test;

import dev.simpleframework.crud.core.ConditionType;
import dev.simpleframework.crud.core.QueryConditions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QueryConditions DSL 构建器单元测试。
 * 验证 AND/OR 工厂、add() 多态推断、同名字段去重、嵌套条件等核心逻辑。
 */
public class QueryConditionsTest {

    /** 场景：创建 AND 条件组。验证点：type=AND, isEmpty=true。 */
    @Test
    public void testAnd_shouldCreateAndType() {
        QueryConditions c = QueryConditions.and();
        assertEquals("AND", c.getType());
        assertTrue(c.isEmpty());
    }

    /** 场景：创建 OR 条件组。验证点：type=OR。 */
    @Test
    public void testOr_shouldCreateOrType() {
        QueryConditions c = QueryConditions.or();
        assertEquals("OR", c.getType());
    }

    /**
     * 场景：add(fieldName, value) 自动推断条件类型。
     * 验证点：String value → equal；Collection → in；Map → json_contains。
     * 为什么测：这是用户最常用的快捷 API，类型推断正确性直接影响 SQL 正确性。
     */
    @Test
    public void testAddStringValue_shouldInferEqual() {
        QueryConditions c = QueryConditions.and().add("name", "Zhang");
        assertEquals(1, c.getFields().size());
        assertEquals("name", c.getFields().get(0).getName());
        assertEquals(ConditionType.equal, c.getFields().get(0).getType());
        assertEquals("Zhang", c.getFields().get(0).getValue());
    }

    /** 场景：add(fieldName, Collection) → 自动推断为 IN 条件。 */
    @Test
    public void testAddCollectionValue_shouldInferIn() {
        QueryConditions c = QueryConditions.and().add("ids", Arrays.asList(1, 2, 3));
        assertEquals(ConditionType.in, c.getFields().get(0).getType());
    }

    /** 场景：add(fieldName, type, null)。验证点：null 值不抛异常，value=null（动态条件跳过）。 */
    @Test
    public void testAddNullValue_shouldNotThrow() {
        QueryConditions c = QueryConditions.and().add("name", ConditionType.equal, (Object) null);
        assertEquals(1, c.getFields().size());
        assertNull(c.getFields().get(0).getValue());
    }

    /**
     * 场景：同一字段添加两次条件。
     * 验证点：key 自动去重——第一个 "name"，第二个 "name1"。
     * 为什么测：MyBatis 参数 Map 不能有重复 key，后缀数字机制是唯一性保证。
     */
    @Test
    public void testAddSameFieldName_shouldAppendSuffixToKey() {
        QueryConditions c = QueryConditions.and()
                .add("name", "a")
                .add("name", "b");
        assertEquals(2, c.getFields().size());
        assertEquals("name", c.getFields().get(0).getKey());
        assertEquals("name1", c.getFields().get(1).getKey());
    }

    /**
     * 场景：嵌套条件组——add(subConditions)。
     * 验证点：子条件进入 subConditions 列表而非 fields。
     * 为什么测：嵌套条件生成括号分组 SQL，数据结构上必须区分叶子条件和子条件。
     */
    @Test
    public void testAddNestedConditions_shouldAddAsSubCondition() {
        QueryConditions sub = QueryConditions.or().add("age", 18);
        QueryConditions c = QueryConditions.and().add(sub);
        assertEquals(1, c.getSubConditions().size());
        assertEquals(0, c.getFields().size());
    }

    /** 场景：add(null)。验证点：不抛异常，subConditions 保持空。 */
    @Test
    public void testAddNullSubCondition_shouldNotThrow() {
        QueryConditions c = QueryConditions.and().add((QueryConditions) null);
        assertEquals(0, c.getSubConditions().size());
    }

    /** 场景：getConditionData() 递归收集所有字段值。验证点：父子条件的字段全部展平到一个 Map。 */
    @Test
    public void testGetConditionData_shouldFlattenFieldsAndSubConditions() {
        QueryConditions c = QueryConditions.and()
                .add("name", "Zhang")
                .add(QueryConditions.or().add("age", 18));
        assertEquals(2, c.getConditionData().size());
    }

    /** 场景：isEmpty() 判断。验证点：新实例 true，add 后 false。 */
    @Test
    public void testIsEmpty_shouldReturnTrueForNewInstance() {
        assertTrue(QueryConditions.and().isEmpty());
        assertFalse(QueryConditions.and().add("name", "Zhang").isEmpty());
    }

}
