package dev.simpleframework.crud.core;

import dev.simpleframework.core.util.Classes;
import dev.simpleframework.core.util.Functions;
import dev.simpleframework.core.util.SerializedFunction;
import dev.simpleframework.core.util.Strings;
import dev.simpleframework.crud.annotation.Condition;
import dev.simpleframework.crud.exception.ModelExecuteException;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class QueryConditions {

    /**
     * key: 类字段名
     * value: 条件信息
     */
    private final Map<String, List<ConditionInfo>> conditions = new LinkedHashMap<>();

    public static QueryConditions of() {
        return new QueryConditions();
    }

    /**
     * 解析类中带有 @Condition 注解的属性构建条件对象
     *
     * @param annotation 带有 @Condition 注解的类实例
     * @return 条件对象
     */
    public static QueryConditions fromAnnotation(Object annotation) {
        return new QueryConditions().addFromAnnotation(annotation);
    }

    /**
     * 聚合多个条件对象
     *
     * @param conditions 条件列表
     * @return 条件列表为 null 时返回一个空条件，不为 null 时返回第一个条件
     */
    public static QueryConditions combineConditions(QueryConditions... conditions) {
        if (conditions == null || conditions.length == 0) {
            return QueryConditions.of();
        }
        return conditions[0].combine(conditions);
    }

    /**
     * 获取条件数据
     * key: 根据 {@link #conditions} 的 key 获取 {@link ConditionInfo#getKey(String)} 的结果值
     * value: ConditionInfo 的值
     *
     * @return 条件数据
     */
    public Map<String, Object> getConditionData() {
        Map<String, Object> result = new HashMap<>(8);
        for (Map.Entry<String, List<ConditionInfo>> entry : this.conditions.entrySet()) {
            ConditionInfo.append(result, entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 获取条件信息列表
     *
     * @param fieldName 类字段名
     * @return 条件信息列表
     */
    public List<ConditionInfo> getConditionInfos(String fieldName) {
        return this.conditions.getOrDefault(fieldName, new ArrayList<>());
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
        annotationFieldConditions(annotation).forEach((fieldName, fieldConditions) -> {
            fieldConditions.forEach(fieldCondition -> {
                this.add(fieldName, fieldCondition.getType(), fieldCondition.getValue());
            });
        });
        return this;
    }

    /**
     * 添加条件
     *
     * @param conditions 另一条件对象
     * @return this
     */
    public QueryConditions add(QueryConditions conditions) {
        if (conditions == this) {
            return this;
        }
        conditions.getConditions().forEach((fieldName, fieldConditions) -> {
            fieldConditions.forEach(fieldCondition -> {
                this.add(fieldName, fieldCondition.getType(), fieldCondition.getValue());
            });
        });
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
        List<ConditionInfo> infos = this.conditions.computeIfAbsent(fieldName, k -> new ArrayList<>());
        Object value = transToValue(conditionType, values);
        ConditionInfo info = ConditionInfo.of(infos.size(), conditionType, value);
        infos.add(info);
        return this;
    }

    private QueryConditions combine(QueryConditions... conditions) {
        for (QueryConditions condition : conditions) {
            if (condition == this) {
                continue;
            }
            this.add(condition);
        }
        return this;
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
        if (type == ConditionType.in || type == ConditionType.not_in) {
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
    private static Map<String, List<ConditionInfo>> annotationFieldConditions(Object annotation) {
        if (annotation == null) {
            return Collections.emptyMap();
        }
        Class<?> annotationClass = annotation.getClass();
        List<Field> fields = CONDITION_CACHES.computeIfAbsent(annotationClass,
                c -> Classes.getFieldsByAnnotations(annotationClass, Condition.class, dev.simpleframework.crud.annotation.Conditions.class));
        if (fields.isEmpty()) {
            throw new ModelExecuteException("Can not found any field declared by @Condition from " + annotationClass.getName());
        }
        Map<String, List<ConditionInfo>> result = new LinkedHashMap<>();
        for (Field field : fields) {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            for (Condition conditionData : field.getAnnotationsByType(Condition.class)) {
                String fieldName = conditionData.field();
                if (Strings.isBlank(fieldName)) {
                    fieldName = field.getName();
                }
                Object conditionValue = field.get(annotation);
                if (conditionValue == null) {
                    String defaultValue = conditionData.defaultValueIfNull();
                    if (Strings.hasText(defaultValue)) {
                        conditionValue = Strings.cast(defaultValue, field.getType());
                    }
                }
                ConditionInfo condition = ConditionInfo.of(conditionData.type(), conditionValue);
                result.computeIfAbsent(fieldName, f -> new ArrayList<>())
                        .add(condition);
            }
            if (!accessible) {
                field.setAccessible(false);
            }
        }
        return result;
    }

    @Data
    public static class ConditionInfo implements Serializable {
        private int index;
        private ConditionType type;
        private Object value;

        public static ConditionInfo of(ConditionType type, Object value) {
            return of(0, type, value);
        }

        public static ConditionInfo of(int index, ConditionType type, Object value) {
            ConditionInfo result = new ConditionInfo();
            result.setIndex(index);
            result.setType(type);
            result.setValue(value);
            return result;
        }

        public String getKey(String fieldName) {
            return fieldName + this.getIndex();
        }

        static void append(Map<String, Object> result, String fieldName, List<ConditionInfo> conditions) {
            for (ConditionInfo condition : conditions) {
                result.put(condition.getKey(fieldName), condition.getValue());
            }
        }

    }

}
