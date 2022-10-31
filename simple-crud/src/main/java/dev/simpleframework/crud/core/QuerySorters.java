package dev.simpleframework.crud.core;

import dev.simpleframework.util.Functions;
import dev.simpleframework.util.SerializedFunction;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class QuerySorters {
    @Getter
    private final Map<String, Boolean> items = new LinkedHashMap<>();

    public static QuerySorters of() {
        return new QuerySorters();
    }

    public static QuerySorters asc(String... fieldNames) {
        return of().addAsc(fieldNames);
    }

    public static <T, R> QuerySorters asc(SerializedFunction<T, R> fieldNameFunc) {
        return of().addAsc(fieldNameFunc);
    }

    public static QuerySorters desc(String... fieldNames) {
        return of().addDesc(fieldNames);
    }

    public static <T, R> QuerySorters desc(SerializedFunction<T, R> fieldNameFunc) {
        return of().addDesc(fieldNameFunc);
    }

    public QuerySorters addAsc(String... fieldNames) {
        for (String fieldName : fieldNames) {
            items.put(fieldName, true);
        }
        return this;
    }

    public <T, R> QuerySorters addAsc(SerializedFunction<T, R> fieldNameFunc) {
        String fieldName = Functions.getLambdaFieldName(fieldNameFunc);
        return this.addAsc(fieldName);
    }

    public QuerySorters addDesc(String... fieldNames) {
        for (String fieldName : fieldNames) {
            items.put(fieldName, false);
        }
        return this;
    }

    public <T, R> QuerySorters addDesc(SerializedFunction<T, R> fieldNameFunc) {
        String fieldName = Functions.getLambdaFieldName(fieldNameFunc);
        return this.addDesc(fieldName);
    }

}
