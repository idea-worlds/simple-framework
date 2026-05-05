package dev.simpleframework.crud.test;

import dev.simpleframework.crud.core.QuerySorters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuerySorters 查询排序器单元测试。
 * 验证 ASC/DESC 标记、多字段排序顺序保持。
 */
public class QuerySortersTest {

    /** 场景：单字段升序。验证点：items[name]=true（true=ASC）。 */
    @Test
    public void testAsc_shouldMarkAsTrue() {
        QuerySorters sorters = QuerySorters.asc("name");
        assertTrue(sorters.getItems().get("name"));
    }

    /** 场景：单字段降序。验证点：items[name]=false（false=DESC）。 */
    @Test
    public void testDesc_shouldMarkAsFalse() {
        QuerySorters sorters = QuerySorters.desc("name");
        assertFalse(sorters.getItems().get("name"));
    }

    /**
     * 场景：多字段排序（如 ORDER BY name ASC, age ASC）。
     * 验证点：items 保持插入顺序（LinkedHashMap），两个字段均为 true。
     * 为什么测：多字段排序顺序决定 SQL ORDER BY 子句顺序，不可乱序。
     */
    @Test
    public void testAddMultipleAsc_shouldPreserveOrder() {
        QuerySorters sorters = QuerySorters.of().addAsc("name").addAsc("age");
        assertEquals(2, sorters.getItems().size());
        assertTrue(sorters.getItems().get("name"));
        assertTrue(sorters.getItems().get("age"));
    }

    /** 场景：混合升降序（name ASC, age DESC）。验证点：各自标记正确。 */
    @Test
    public void testMixedAscDesc_shouldWork() {
        QuerySorters sorters = QuerySorters.of().addAsc("name").addDesc("age");
        assertTrue(sorters.getItems().get("name"));
        assertFalse(sorters.getItems().get("age"));
    }

}
