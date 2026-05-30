package dev.simpleframework.crud.test;

import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.core.FieldOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldOptionsTest {

    @Test
    public void testName_shouldSetColumnName() {
        FieldOptions config = new FieldOptions().name("user_name");
        assertEquals("user_name", config.getColumnName());
    }

    @Test
    public void testId_shouldSetIdType() {
        FieldOptions config = new FieldOptions().id(Id.Type.UUID32);
        assertEquals(Id.Type.UUID32, config.getIdType());
    }

    @Test
    public void testInsertable_shouldSetInsertable() {
        FieldOptions config = new FieldOptions().insertable(false);
        assertFalse(config.getInsertable());
    }

    @Test
    public void testUpdatable_shouldSetUpdatable() {
        FieldOptions config = new FieldOptions().updatable(false);
        assertFalse(config.getUpdatable());
    }

    @Test
    public void testSelectable_shouldSetSelectable() {
        FieldOptions config = new FieldOptions().selectable(false);
        assertFalse(config.getSelectable());
    }

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

    @Test
    public void testAutoFillWithAnnotationClass_shouldCreateSyntheticAnnotation() {
        FieldOptions config = new FieldOptions().autoFill(dev.simpleframework.crud.annotation.DataOperateDate.class);
        assertNotNull(config.getAutoFill());
    }

}
