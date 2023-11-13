package dev.simpleframework.crud.core;

import dev.simpleframework.util.SerializedFunction;
import lombok.Data;

import java.util.Collection;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class QueryConfig {

    /**
     * 查询字段
     */
    private QueryFields fields = QueryFields.of();
    /**
     * 条件
     */
    private QueryConditions conditions = QueryConditions.of();
    /**
     * 排序
     */
    private final QuerySorters sorters = QuerySorters.of();

    public static QueryConfig of() {
        return new QueryConfig();
    }

    public static QueryConfig combineConfigs(QueryConfig... configs) {
        if (configs == null || configs.length == 0) {
            return QueryConfig.of();
        }
        return configs[0].combine(configs);
    }

    public QueryConfig addField(String... fieldNames) {
        this.fields.add(fieldNames);
        return this;
    }

    public <T, R> QueryConfig addField(SerializedFunction<T, R> fieldNameFunc) {
        this.fields.add(fieldNameFunc);
        return this;
    }

    public QueryConfig addField(Collection<String> fieldNames) {
        this.fields.add(fieldNames);
        return this;
    }

    public QueryConfig addField(QueryFields fields) {
        this.fields.add(fields);
        return this;
    }

    public QueryConfig addCondition(String fieldName, Object value) {
        this.conditions.add(fieldName, value);
        return this;
    }

    public <T, R> QueryConfig addCondition(SerializedFunction<T, R> fieldNameFunc, Object value) {
        this.conditions.add(fieldNameFunc, value);
        return this;
    }

    public QueryConfig addCondition(String fieldName, ConditionType conditionType, Object... values) {
        this.conditions.add(fieldName, conditionType, values);
        return this;
    }

    public <T, R> QueryConfig addCondition(SerializedFunction<T, R> fieldNameFunc, ConditionType conditionType, Object... values) {
        this.conditions.add(fieldNameFunc, conditionType, values);
        return this;
    }

    public QueryConfig addCondition(QueryConditions conditions) {
        this.conditions.add(conditions);
        return this;
    }

    public QueryConfig addSorter(QuerySorters sorter) {
        this.sorters.getItems().putAll(sorter.getItems());
        return this;
    }

    private QueryConfig combine(QueryConfig... configs) {
        for (QueryConfig config : configs) {
            if (config == this) {
                continue;
            }
            this.addField(config.getFields());
            this.addCondition(config.getConditions());
            this.addSorter(config.getSorters());
        }
        return this;
    }

}
