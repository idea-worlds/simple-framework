package dev.simpleframework.crud.test.support;

/**
 * 测试用的 ModelField 桩。
 */
public class TestModelField implements dev.simpleframework.crud.ModelField<Object> {

    private final String columnName;
    private final String fieldName;
    private final Class<?> fieldComponentType;

    public TestModelField(String fieldName, String columnName, Class<?> fieldComponentType) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.fieldComponentType = fieldComponentType;
    }

    public TestModelField(String columnName, Class<?> fieldComponentType) {
        this(columnName, columnName, fieldComponentType);
    }

    @Override
    public String columnName() { return columnName; }

    @Override
    public String fieldName() { return fieldName; }

    @Override
    public Class<?> fieldType() { return fieldComponentType; }

    @Override
    public Class<?> fieldComponentType() { return fieldComponentType; }

    @Override
    public Object getValue(Object model) { return null; }

    @Override
    public void setValue(Object model, Object value) {}

    @Override
    public void autoFillValue(Object model) {}

    @Override
    public boolean insertable() { return true; }

    @Override
    public boolean updatable() { return true; }

    @Override
    public boolean selectable() { return true; }

}
