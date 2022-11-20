package dev.simpleframework.crud.info.clazz;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.annotation.Table;
import dev.simpleframework.crud.core.ModelConfiguration;
import dev.simpleframework.crud.core.ModelNameStrategy;
import dev.simpleframework.crud.info.AbstractModelInfo;
import dev.simpleframework.crud.util.Constants;
import dev.simpleframework.util.Classes;
import dev.simpleframework.util.Strings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class ClassModelInfo<T> extends AbstractModelInfo<T> {
    /**
     * 默认表主键的类字段名
     */
    private static final String DEFAULT_ID_FIELD = "id";

    public ClassModelInfo(Class<T> modelClass, ModelConfiguration modelConfig) {
        super(modelClass, modelConfig, obtainModelName(modelClass, modelConfig.tableNameStrategy()));
        List<ModelField<T>> modelFields = obtainFields(modelClass, modelConfig.columnNameStrategy());
        super.addFields(modelFields);
        super.setId(obtainModelIdFieldName(modelFields), null);
    }

    /**
     * 获取模型名，默认为类名转为下划线
     * 当类有 @Table 注解时：
     * 若 name() 有值，则表名为 name()
     * 若 schema() 有值，则表名为 schema().表名
     *
     * @see dev.simpleframework.crud.annotation.Table
     * @see javax.persistence.Table
     */
    private static <M> String obtainModelName(Class<M> clazz, ModelNameStrategy nameType) {
        BiFunction<String, String, String> nameTrans = (modelName, definedName) -> {
            if (Strings.hasText(definedName)) {
                modelName = definedName;
            }
            return modelName;
        };
        BiFunction<String, String, String> schemaTrans = (modelName, definedSchema) -> {
            if (Strings.hasText(definedSchema)) {
                modelName = definedSchema + "." + modelName;
            }
            return modelName;
        };

        String modelName = nameType.trans(clazz.getSimpleName());
        Table crudTable = clazz.getAnnotation(Table.class);
        if (crudTable != null) {
            modelName = nameTrans.apply(modelName, crudTable.name());
            modelName = schemaTrans.apply(modelName, crudTable.schema());
        } else if (Constants.jpaPresent) {
            javax.persistence.Table jpaTable = clazz.getAnnotation(javax.persistence.Table.class);
            if (jpaTable != null) {
                modelName = nameTrans.apply(modelName, jpaTable.name());
                modelName = schemaTrans.apply(modelName, jpaTable.schema());
            }
        }
        return modelName;
    }

    /**
     * 获取类的字段列表
     */
    private static <M> List<ModelField<M>> obtainFields(Class<M> modelClass, ModelNameStrategy nameType) {
        Predicate<Field> fieldFilter = field -> {
            int modifiers = field.getModifiers();
            // 过滤掉静态字段
            boolean isStatic = Modifier.isStatic(modifiers);
            // 过滤掉 transient 关键字修饰的字段
            boolean isTransient = Modifier.isTransient(modifiers);
            if (!isTransient) {
                // 过滤掉 @Transient 注解的字段
                isTransient = Constants.jpaPresent && field.isAnnotationPresent(javax.persistence.Transient.class);
            }
            return !isStatic && !isTransient;
        };
        return Classes.getFields(modelClass, fieldFilter)
                .stream()
                .map(field -> new ClassModelField<>(modelClass, field, nameType))
                .collect(Collectors.toList());
    }

    /**
     * 获取主键字段
     * 默认主键类字段名为 id，可通过注解 @Id 声明类字段为主键
     *
     * @see dev.simpleframework.crud.annotation.Id
     * @see javax.persistence.Id
     */
    private static <M> String obtainModelIdFieldName(List<ModelField<M>> modelFields) {
        for (ModelField<M> modelField : modelFields) {
            ClassModelField<M> field = (ClassModelField<M>) modelField;
            if (field.getField().isAnnotationPresent(Id.class)) {
                return field.fieldName();
            }
        }
        ClassModelField<M> idField = null;
        for (ModelField<M> modelField : modelFields) {
            ClassModelField<M> field = (ClassModelField<M>) modelField;
            if (Constants.jpaPresent && field.getField().isAnnotationPresent(javax.persistence.Id.class)) {
                return field.fieldName();
            }
            if (DEFAULT_ID_FIELD.equals(modelField.fieldName())) {
                idField = field;
            }
        }
        return idField == null ? null : idField.fieldName();
    }

}
