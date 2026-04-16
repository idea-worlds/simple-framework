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
    public static final String KEY_NAME = "data";
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

    /**
     * 是否无查询条件
     */
    public boolean isEmpty() {
        return this.fields.isEmpty() && this.subConditions.isEmpty();
    }

    public QueryConditions add(String fieldName, Object value) {
        if (value instanceof ConditionType) {
            return this.add(fieldName, (ConditionType) value, (Object) null);
        }
        if (value instanceof Collection) {
            return this.add(fieldName, ConditionType.in, value);
        }
        if (value instanceof Map) {
            return this.add(fieldName, ConditionType.json_contains, value);
        }
        return this.add(fieldName, ConditionType.equal, value);
    }

    public <T, R> QueryConditions add(SerializedFunction<T, R> fieldNameFunc, Object value) {
        String fieldName = Functions.getLambdaFieldName(fieldNameFunc);
        return this.add(fieldName, value);
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
     * 将另一个条件组作为子条件（括号分组）追加到当前条件中。
     * <p>
     * 合并前会调用 {@link #flushFieldKey} 重新统一字段计数器，确保同名字段在参数 Map
     * 中的 key 不冲突（第 2 个 name 条件的 key 变为 {@code name1}，以此类推）。
     *
     * @param conditions 子条件，不能为当前对象自身
     * @return this
     */
    public synchronized QueryConditions add(QueryConditions conditions) {
        if (conditions == null || conditions == this) {
            return this;
        }
        conditions.flushFieldKey(this.fieldSize);
        this.subConditions.add(conditions);
        return this;
    }

    /**
     * 添加一个字段条件。
     * <p>
     * 同名字段去重规则：第一个 {@code name} 的 key 仍为 {@code name}，
     * 后续同名条件的 key 依次为 {@code name1}、{@code name2}……
     * 这些 key 最终作为 MyBatis 参数 Map（{@code data.xxx}）中的唯一键。
     *
     * @param fieldName     字段名（模型类中的 Java 字段名）
     * @param conditionType 条件类型，null 时默认为 {@link ConditionType#equal}
     * @param values        条件值，null/空数组时该条件在 SQL 中会被跳过（动态条件）
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
            // 同名字段后缀数字，避免参数 Map key 冲突
            field.setKey(fieldName + size);
        }

        this.fields.add(field);
        return this;
    }

    /**
     * 将子条件合并进父条件的字段计数器，并修正所有字段的 key。
     * <p>
     * 当子条件被 {@link #add(QueryConditions)} 合并时调用此方法，
     * 递归地将子条件及其所有嵌套子条件中重复的字段名都加上数字后缀，
     * 使整棵条件树的参数 key 全局唯一。
     */
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

    /**
     * 将可变参数规范化为条件所需的值对象。
     * <p>
     * 规范化规则：
     * <ul>
     *   <li>集合类型条件（in/not_in/array_xx/json_exist_key_xx）：展平所有参数为单个 List；</li>
     *   <li>多值传入 {@code json_exist_key} 时自动升级为 {@code json_exist_key_all}；</li>
     *   <li>单值非集合条件：直接返回 {@code values[0]}；</li>
     *   <li>多值非集合条件：返回 {@code Arrays.asList(values)}。</li>
     * </ul>
     */
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
        if (type == ConditionType.json_exist_key && valuesLength > 1) {
            type = ConditionType.json_exist_key_all;
        }
        if (type == ConditionType.in
                || type == ConditionType.not_in
                || type == ConditionType.array_contains
                || type == ConditionType.array_contained_by
                || type == ConditionType.array_overlap
                || type == ConditionType.json_exist_key_any
                || type == ConditionType.json_exist_key_all) {
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

    /**
     * 解析对象中所有带 {@link Condition}（含 {@link Conditions} 容器）注解的字段，
     * 构建对应的 {@link QueryConditionField} 列表。
     * <p>
     * 字段反射结果按类缓存（{@link #CONDITION_CACHES}），同一类只扫描一次。
     * 每个字段可声明多个 {@code @Condition}，均会独立生成条件项。
     * 字段值为 null 时，若注解指定了 {@code defaultValueIfNull}，则自动转换为目标类型填充。
     */
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
