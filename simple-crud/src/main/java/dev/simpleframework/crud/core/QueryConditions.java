package dev.simpleframework.crud.core;

import dev.simpleframework.crud.annotation.Condition;
import dev.simpleframework.crud.annotation.Conditions;
import dev.simpleframework.crud.exception.ModelExecuteException;
import dev.simpleframework.util.Classes;
import dev.simpleframework.util.Functions;
import dev.simpleframework.util.SerializedFunction;
import dev.simpleframework.util.Strings;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class QueryConditions {
    public static final String TYPE_AND = "AND";
    public static final String TYPE_OR = "OR";

    private final String type;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Integer> fieldSize = new HashMap<>();
    private final List<QueryConditionField> fields = new ArrayList<>();
    private final List<QueryConditions> subConditions = new ArrayList<>();

    public static QueryConditions and() {
        return new QueryConditions(TYPE_AND);
    }

    public static QueryConditions or() {
        return new QueryConditions(TYPE_OR);
    }

    private QueryConditions(String type) {
        this.type = type;
    }

    /**
     * 解析类中带有 @Condition 注解的属性构建条件对象
     *
     * @param annotation 带有 @Condition 注解的类实例
     * @return 条件对象
     */
    public static QueryConditions fromAnnotation(Object annotation) {
        return QueryConditions.and().addFromAnnotation(annotation);
    }

    /**
     * 聚合多个条件对象
     *
     * @param conditions 条件列表
     * @return 条件列表为 null 时返回一个空条件，不为 null 时返回第一个条件
     */
    public static QueryConditions combineConditions(QueryConditions... conditions) {
        if (conditions == null || conditions.length == 0) {
            return QueryConditions.and();
        }
        if (conditions.length == 1) {
            return conditions[0];
        }
        QueryConditions result = QueryConditions.and();
        for (QueryConditions condition : conditions) {
            result.add(condition);
        }
        return result;
    }

    /**
     * 获取条件数据
     */
    public Map<String, Object> getConditionData() {
        Map<String, Object> result = new HashMap<>(8);
        for (QueryConditionField field : this.fields) {
            result.put(field.getKey(), field.getValue());
        }
        for (QueryConditions sub : this.subConditions) {
            result.putAll(sub.getConditionData());
        }
        return result;
    }

    public QueryConditions add(String fieldName, Object value) {
        if (value instanceof ConditionType) {
            return this.add(fieldName, (ConditionType) value, (Object) null);
        }
        if (value instanceof Collection) {
            return this.add(fieldName, ConditionType.in, value);
        } else {
            return this.add(fieldName, ConditionType.equal, value);
        }
    }

    public <T, R> QueryConditions add(SerializedFunction<T, R> fieldNameFunc, Object value) {
        if (value instanceof ConditionType) {
            return this.add(fieldNameFunc, (ConditionType) value, (Object) null);
        }
        if (value instanceof Collection) {
            return this.add(fieldNameFunc, ConditionType.in, value);
        } else {
            return this.add(fieldNameFunc, ConditionType.equal, value);
        }
    }

    public <T, R> QueryConditions add(SerializedFunction<T, R> fieldNameFunc, ConditionType conditionType, Object... values) {
        String fieldName = Functions.getLambdaFieldName(fieldNameFunc);
        return this.add(fieldName, conditionType, values);
    }

    public QueryConditions addFromAnnotation(Object annotation) {
        annotationConditionFields(annotation)
                .forEach(field -> this.add(field.getName(), field.getType(), field.getValue()));
        return this;
    }

    /**
     * 添加条件
     *
     * @param conditions 另一条件对象
     * @return this
     */
    public synchronized QueryConditions add(QueryConditions conditions) {
        return this.add(conditions, true);
    }

    /**
     * 添加条件
     *
     * @param conditions 另一条件对象
     * @param flushKey   是否重置条件字段的key值
     * @return this
     */
    public synchronized QueryConditions add(QueryConditions conditions, boolean flushKey) {
        if (conditions == null || conditions == this) {
            return this;
        }
        if (flushKey) {
            conditions.flushFieldKey(this.fieldSize);
        }
        this.subConditions.add(conditions);
        return this;
    }

    /**
     * 添加条件
     *
     * @param fieldName     字段名
     * @param conditionType 条件类型，默认”相等“
     * @param values        条件值
     * @return this
     */
    public synchronized QueryConditions add(String fieldName, ConditionType conditionType, Object... values) {
        if (conditionType == null) {
            conditionType = ConditionType.equal;
        }
        int size = this.fieldSize.getOrDefault(fieldName, 0);
        this.fieldSize.put(fieldName, size + 1);

        Object value = transToValue(conditionType, values);
        QueryConditionField field = QueryConditionField.of(fieldName, conditionType, value);
        if (size > 0) {
            field.setKey(fieldName + size);
        }

        this.fields.add(field);
        return this;
    }

    private void flushFieldKey(Map<String, Integer> fieldSize) {
        this.fieldSize = fieldSize;
        for (QueryConditionField field : this.fields) {
            String fieldName = field.getName();
            int size = fieldSize.getOrDefault(fieldName, 0);
            fieldSize.put(fieldName, size + 1);
            if (size > 0) {
                field.setKey(fieldName + size);
            }
        }
        for (QueryConditions sub : this.subConditions) {
            sub.flushFieldKey(fieldSize);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object transToValue(ConditionType type, Object... values) {
        if (values == null) {
            return null;
        }
        values = Arrays.stream(values).filter(Objects::nonNull).toArray();
        int valuesLength = values.length;
        if (valuesLength == 0) {
            return null;
        }
        if (type == ConditionType.in
                || type == ConditionType.not_in
                || type == ConditionType.array_contains
                || type == ConditionType.array_contained_by
                || type == ConditionType.array_overlap) {
            List<Object> result = new ArrayList<>();
            for (Object temp : values) {
                if (temp instanceof Collection) {
                    result.addAll((Collection<Object>) temp);
                } else {
                    result.add(temp);
                }
            }
            return result.isEmpty() ? null : result;
        }
        return valuesLength == 1 ? values[0] : Arrays.asList(values);
    }

    private static final Map<Class<?>, List<Field>> CONDITION_CACHES = new ConcurrentHashMap<>();

    @SneakyThrows
    private static List<QueryConditionField> annotationConditionFields(Object annotation) {
        if (annotation == null) {
            return Collections.emptyList();
        }
        Class<?> annotationClass = annotation.getClass();
        List<Field> fields = CONDITION_CACHES.computeIfAbsent(annotationClass,
                c -> Classes.getFieldsByAnnotations(annotationClass, Condition.class, Conditions.class));
        if (fields.isEmpty()) {
            throw new ModelExecuteException("Can not found any field declared by @Condition from " + annotationClass.getName());
        }
        List<QueryConditionField> result = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            for (Condition condition : field.getAnnotationsByType(Condition.class)) {
                String fieldName = condition.field();
                if (Strings.isBlank(fieldName)) {
                    fieldName = field.getName();
                }
                Object conditionValue = field.get(annotation);
                if (conditionValue == null) {
                    String defaultValue = condition.defaultValueIfNull();
                    if (Strings.hasText(defaultValue)) {
                        conditionValue = Strings.cast(defaultValue, field.getType());
                    }
                }
                result.add(QueryConditionField.of(fieldName, condition.type(), conditionValue));
            }
        }
        return result;
    }

}
