package dev.simpleframework.crud.test;

import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.core.FieldOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldOptions 字段策略覆盖配置测试。
 * FieldOptions 是 FieldCustomizer 的内部 Builder，用于在 POJO 不可修改时声明式覆盖字段行为。
 * 验证所有 setter 方法和链式调用 API。
 */
public class FieldOptionsTest {

    /** 场景：覆盖列名。验证点：name(column) 设置 columnName。 */
    @Test
    public void testName_shouldSetColumnName() {
        FieldOptions config = new FieldOptions().name("user_name");
        assertEquals("user_name", config.getColumnName());
    }

    /** 场景：覆盖主键策略。验证点：id(Type) 设置 idType。 */
    @Test
    public void testId_shouldSetIdType() {
        FieldOptions config = new FieldOptions().id(Id.Type.UUID32);
        assertEquals(Id.Type.UUID32, config.getIdType());
    }

    /** 场景：控制字段是否参与 INSERT。验证点：insertable(false) 设置对应布尔值。 */
    @Test
    public void testInsertable_shouldSetInsertable() {
        FieldOptions config = new FieldOptions().insertable(false);
        assertFalse(config.getInsertable());
    }

    /** 场景：控制字段是否参与 UPDATE。 */
    @Test
    public void testUpdatable_shouldSetUpdatable() {
        FieldOptions config = new FieldOptions().updatable(false);
        assertFalse(config.getUpdatable());
    }

    /** 场景：控制字段是否参与 SELECT。 */
    @Test
    public void testSelectable_shouldSetSelectable() {
        FieldOptions config = new FieldOptions().selectable(false);
        assertFalse(config.getSelectable());
    }

    /**
     * 场景：链式调用覆盖多个字段属性。
     * 验证点：Fluent API 正确设置全部属性，不会互相覆盖。
     * 为什么测：FieldOptions 设计为 Builder 模式，链式调用是最常用的使用方式。
     */
    @Test
    public void testChain_shouldSupportFluentApi() {
        FieldOptions config = new FieldOptions()
                .name("user_name")
                .id(Id.Type.SNOWFLAKE)
                .insertable(true)
                .updatable(false)
                .selectable(true);
        assertEquals("user_name", config.getColumnName());
        assertEquals(Id.Type.SNOWFLAKE, config.getIdType());
        assertTrue(config.getInsertable());
        assertFalse(config.getUpdatable());
        assertTrue(config.getSelectable());
    }

}
