package dev.simpleframework.crud.core;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.util.Functions;
import dev.simpleframework.util.SerializedFunction;

import java.util.*;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class QueryFields {
    private static final String SELECT_ALL_FIELDS = "*";
    /**
     * 要查询的类字段名
     * 空或含 * 表示查全部
     */
    private final Set<String> fieldNames = new HashSet<>();

    public static QueryFields of() {
        return new QueryFields();
    }

    public static QueryFields combineFields(QueryFields... fields) {
        if (fields == null || fields.length == 0) {
            return QueryFields.of();
        }
        return fields[0].combine(fields);
    }

    /**
     * 获取查询字段
     * 根据 field.fieldName() 匹配 {@link #fieldNames}
     * 当 {@link #fieldNames} 含 * 时，表示查全部
     *
     * @param fields 要匹配的字段列表
     * @return 查询字段
     */
    public List<? extends ModelField<?>> find(List<? extends ModelField<?>> fields) {
        if (this.fieldNames.isEmpty() || this.fieldNames.contains(SELECT_ALL_FIELDS)) {
            return fields;
        }
        List<ModelField<?>> result = new ArrayList<>();
        for (ModelField<?> field : fields) {
            if (this.fieldNames.contains(field.fieldName())) {
                result.add(field);
            }
        }
        return result;
    }

    /**
     * 添加要查询的字段
     *
     * @param fieldNames 当为 * 时，表示要查全部字段
     * @return this
     */
    public QueryFields add(String... fieldNames) {
        if (fieldNames == null) {
            return this;
        }
        return this.add(Arrays.asList(fieldNames));
    }

    /**
     * 添加要查询的字段
     */
    public <T, R> QueryFields add(SerializedFunction<T, R> fieldNameFunc) {
        String fieldName = Functions.getLambdaFieldName(fieldNameFunc);
        return this.add(fieldName);
    }

    public QueryFields add(Collection<String> fieldNames) {
        if (fieldNames.isEmpty()) {
            return this;
        }
        this.fieldNames.addAll(fieldNames);
        return this;
    }

    public QueryFields add(QueryFields fields) {
        this.add(fields.fieldNames);
        return this;
    }

    private QueryFields combine(QueryFields... fields) {
        for (QueryFields field : fields) {
            if (field == this) {
                continue;
            }
            this.fieldNames.addAll(field.fieldNames);
        }
        return this;
    }

}
