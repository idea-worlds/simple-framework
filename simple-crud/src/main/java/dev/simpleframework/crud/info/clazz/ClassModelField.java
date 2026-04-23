package dev.simpleframework.crud.info.clazz;

import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.core.FieldConfig;
import dev.simpleframework.crud.info.AbstractModelField;
import dev.simpleframework.crud.util.ModelCache;
import dev.simpleframework.util.Classes;
import dev.simpleframework.util.Strings;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class ClassModelField<T> extends AbstractModelField<T> {
    private final Class<T> modelClass;
    private final Field field;

    ClassModelField(Class<T> modelClass, Field field) {
        this.modelClass = modelClass;
        this.field = field;
        field.setAccessible(true);
        String fieldName = field.getName();
        FieldConfig config = this.buildConfig();
        if (config.getColumnName() == null) {
            config.name(Strings.camelToUnderline(fieldName).toUpperCase());
        }
        super.config(config);
        Class<?> fieldType = field.getType();
        Class<?> fieldComponentType = Classes.getGenericClass(field);
        super.setColumn(config.getColumnName(), fieldName, fieldType, fieldComponentType);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public <R> R getValue(T model) {
        return (R) this.field.get(model);
    }

    @SneakyThrows
    @Override
    public void setValue(T model, Object value) {
        this.field.set(model, value);
    }

    Field getField() {
        return this.field;
    }

    private FieldConfig buildConfig() {
        boolean insertable = true;
        boolean updatable = true;
        boolean selectable = true;
        String columnName = null;
        Column column = this.field.getAnnotation(Column.class);
        if (column != null) {
            columnName = column.name();
            insertable = column.insertable();
            updatable = column.updatable();
            selectable = column.selectable();
        }
        Annotation fillAnnotation = Arrays.stream(this.field.getAnnotations())
                .filter(a -> ModelCache.fillStrategy(a.annotationType()) != null)
                .min(Comparator.comparingInt(a -> ModelCache.fillStrategy(a.annotationType()).order()))
                .orElse(null);
        return new FieldConfig()
                .name(columnName)
                .insertable(insertable)
                .updatable(updatable)
                .selectable(selectable)
                .autoFill(fillAnnotation);
    }

}
