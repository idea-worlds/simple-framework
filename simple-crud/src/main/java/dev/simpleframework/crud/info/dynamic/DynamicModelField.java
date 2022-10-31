package dev.simpleframework.crud.info.dynamic;

import dev.simpleframework.crud.exception.FieldDefinitionException;
import dev.simpleframework.crud.info.AbstractModelField;

import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class DynamicModelField extends AbstractModelField<Map<String, Object>> {

    public DynamicModelField(String fieldName, String column) {
        this(fieldName, column, String.class);
    }

    public DynamicModelField(String fieldName, String column, Class<?> fieldType) {
        super();
        super.setColumn(column, fieldName, fieldType);
        super.setInsertable(true);
        super.setUpdatable(true);
        super.setSelectable(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R getValue(Map<String, Object> model) {
        return (R) model.get(super.fieldName());
    }

    @Override
    public void setValue(Map<String, Object> model, Object value) {
        this.validValue(value);
        model.put(super.fieldName(), value);
    }

    @Override
    public void setInsertable(boolean insertable) {
        super.setInsertable(insertable);
    }

    @Override
    public void setUpdatable(boolean updatable) {
        super.setUpdatable(updatable);
    }

    @Override
    public void setSelectable(boolean selectable) {
        super.setSelectable(selectable);
    }

    private void validValue(Object value) {
        if (value == null || super.fieldType().isAssignableFrom(value.getClass())) {
            return;
        }
        String msg = String.format("field [%s] must be [%s]", super.fieldName(), super.fieldType());
        throw new FieldDefinitionException("dynamic model", msg);
    }

}
