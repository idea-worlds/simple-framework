package dev.simpleframework.crud.core;

import lombok.Data;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class QueryConditionField {
    private String key;
    private String name;
    private ConditionType type;
    private Object value;

    public static QueryConditionField of(String fieldName, Object value) {
        return of(fieldName, ConditionType.equal, value);
    }

    public static QueryConditionField of(String fieldName, ConditionType type, Object value) {
        QueryConditionField result = new QueryConditionField();
        result.setKey(fieldName);
        result.setName(fieldName);
        result.setType(type);
        result.setValue(value);
        return result;
    }

}
