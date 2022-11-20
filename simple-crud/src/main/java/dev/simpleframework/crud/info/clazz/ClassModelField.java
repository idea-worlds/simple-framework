package dev.simpleframework.crud.info.clazz;

import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.annotation.Column;
import dev.simpleframework.crud.core.ModelNameStrategy;
import dev.simpleframework.crud.info.AbstractModelField;
import dev.simpleframework.crud.strategy.DataFillStrategy;
import dev.simpleframework.crud.util.Constants;
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

    ClassModelField(Class<T> modelClass, Field field, ModelNameStrategy nameType) {
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
        } else if (Constants.jpaPresent) {
            javax.persistence.Column jpaColumn = this.field.getAnnotation(javax.persistence.Column.class);
            if (jpaColumn != null) {
                columnName = jpaColumn.name();
                insertable = jpaColumn.insertable();
                updatable = jpaColumn.updatable();
            }
        }
        if (Strings.isBlank(columnName)) {
            columnName = nameType.trans(fieldName);
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

        DataFillStrategy fillStrategy = null;
        Object fillStrategyParam = null;
        for (Annotation annotation : this.field.getAnnotations()) {
            DataFillStrategy temp = Models.fillStrategy(annotation.annotationType());
            if (fillStrategy == null || fillStrategy.order() >= temp.order()) {
                fillStrategy = temp;
                fillStrategyParam = fillStrategy.toParam(annotation);
            }
        }
        super.setFillStrategy(fillStrategy, fillStrategyParam);
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
