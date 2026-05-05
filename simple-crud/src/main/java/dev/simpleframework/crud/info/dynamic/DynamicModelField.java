package dev.simpleframework.crud.info.dynamic;

import dev.simpleframework.crud.core.FieldOptions;
import dev.simpleframework.crud.exception.ModelExecuteException;
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
        this(fieldName, column, fieldType, null);
    }

    public DynamicModelField(String fieldName, String column, Class<?> fieldType, Class<?> fieldComponentType) {
        super();
        super.setColumn(column, fieldName, fieldType, fieldComponentType);
        FieldOptions config = new FieldOptions()
                .name(column)
                .insertable(true)
                .updatable(true)
                .selectable(true);
        super.config(config);
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
    public void config(FieldOptions config) {
        super.config(config);
    }

    private void validValue(Object value) {
        if (value == null || super.fieldType().isAssignableFrom(value.getClass())) {
            return;
        }
        String msg = String.format("Type of field [%s] must be [%s]", super.fieldName(), super.fieldType());
        throw new ModelExecuteException("dynamic model", msg);
    }

}
