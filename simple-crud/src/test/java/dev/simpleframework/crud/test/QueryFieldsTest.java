package dev.simpleframework.crud.test;

import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.test.support.TestModelField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QueryFields 查询字段选择器单元测试。
 * 验证 include 字段过滤、空集合/null 防御、多字段组合。
 * 用户用 QueryFields 控制 SELECT 列的子集，减少不必要的数据传输。
 */
public class QueryFieldsTest {

    /**
     * 场景：未指定任何字段（默认查全部）。
     * 验证点：find() 返回全部字段列表。
     * 为什么测：默认行为是 SELECT *，用户不指定字段时不应过滤。
     */
    @Test
    public void testEmpty_shouldReturnAllFields() {
        QueryFields qf = QueryFields.of();
        var fields = Arrays.<dev.simpleframework.crud.ModelField<?>>asList(
                new TestModelField("name", "name", String.class),
                new TestModelField("age", "age", Integer.class)
        );
        List<?> result = qf.find(fields);
        assertEquals(2, result.size());
    }

    /**
     * 场景：指定只查 name 字段。
     * 验证点：find() 只返回 name 字段，age 被过滤。
     */
    @Test
    public void testAddSpecific_shouldFilterFields() {
        QueryFields qf = QueryFields.of().add("name");
        var fields = Arrays.<dev.simpleframework.crud.ModelField<?>>asList(
                new TestModelField("name", "name", String.class),
                new TestModelField("age", "age", Integer.class)
        );
        List<?> result = qf.find(fields);
        assertEquals(1, result.size());
        assertEquals("name", ((dev.simpleframework.crud.ModelField<?>) result.get(0)).fieldName());
    }

    /** 场景：add(null)。验证点：不抛异常，保持原有状态。 */
    @Test
    public void testAddNull_shouldNotThrow() {
        QueryFields qf = QueryFields.of().add((String[]) null);
        assertNotNull(qf);
    }

    /** 场景：add(空集合)。验证点：不改变已有字段列表。 */
    @Test
    public void testAddEmptyCollection_shouldNotChange() {
        QueryFields qf = QueryFields.of().add("name").add(Arrays.asList());
        var fields = Arrays.<dev.simpleframework.crud.ModelField<?>>asList(
                new TestModelField("name", "name", String.class)
        );
        assertEquals(1, qf.find(fields).size());
    }

}
