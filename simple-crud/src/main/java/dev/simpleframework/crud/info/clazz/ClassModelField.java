package dev.simpleframework.crud.info.clazz;

import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.helper.DataFillStrategy;
import dev.simpleframework.crud.info.AbstractModelField;
import dev.simpleframework.crud.util.ModelCache;
import dev.simpleframework.util.Classes;
import dev.simpleframework.util.Strings;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

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

        boolean insertable = true;
        boolean updatable = true;
        boolean selectable = true;
        String columnName = null;
        Column crudColumn = this.field.getAnnotation(Column.class);
        if (crudColumn != null) {
            columnName = crudColumn.name();
            insertable = crudColumn.insertable();
            updatable = crudColumn.updatable();
            selectable = crudColumn.selectable();
        }
        if (Strings.isBlank(columnName)) {
            columnName = Strings.camelToUnderline(fieldName).toUpperCase();
        }
        Class<?> fieldType = field.getType();
        Class<?> fieldComponentType = null;
        if (Collection.class.isAssignableFrom(fieldType)) {
            fieldComponentType = Classes.getGenericClass(field, Object.class);
        }
        super.setColumn(columnName, fieldName, fieldType, fieldComponentType);
        super.setInsertable(insertable);
        super.setUpdatable(updatable);
        super.setSelectable(selectable);

        DataFillStrategy strategy = null;
        Object strategyParam = null;
        for (Annotation annotation : this.field.getAnnotations()) {
            DataFillStrategy _strategy = ModelCache.fillStrategy(annotation.annotationType());
            if (strategy == null ||
                    (_strategy != null && strategy.order() >= _strategy.order())) {
                strategy = _strategy;
            }
            if (strategy != null) {
                strategyParam = strategy.toParam(annotation);
            }
        }
        super.setFillStrategy(strategy, strategyParam);
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

}
