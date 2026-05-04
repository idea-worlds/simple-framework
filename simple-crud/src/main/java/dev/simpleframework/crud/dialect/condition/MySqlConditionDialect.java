package dev.simpleframework.crud.dialect.condition;

import dev.simpleframework.crud.ModelField;

/**
 * MySQL 条件方言。
 * 数组条件映射到 MySQL JSON 函数（适用 MySQL 5.7+），JSON 条件使用 {@code JSON_CONTAINS} / {@code JSON_CONTAINS_PATH} 函数。
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class MySqlConditionDialect extends SqlConditionDialect {
    public static final MySqlConditionDialect DEFAULT = new MySqlConditionDialect();

    // ──── 数组条件 → JSON 函数 ────

    @Override
    public String arrayContains(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return "JSON_CONTAINS(" + column + ", " + value + ")";
    }

    @Override
    public String arrayContainedBy(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return "JSON_CONTAINS(" + value + ", " + column + ")";
    }

    @Override
    public String arrayOverlap(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return "JSON_OVERLAPS(" + column + ", " + value + ")";
    }

    // ──── JSON 条件 → JSON 函数 ────

    @Override
    public String jsonContains(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return "JSON_CONTAINS(" + column + ", " + value + ")";
    }

    @Override
    public String jsonContainedBy(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return "JSON_CONTAINS(" + value + ", " + column + ")";
    }

    @Override
    public String jsonExistKey(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return "JSON_CONTAINS_PATH(" + column + ", 'one', " + value + ")";
    }

    @Override
    public String jsonExistKeyAny(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return "JSON_CONTAINS_PATH(" + column + ", 'one', " + value + ")";
    }

    @Override
    public String jsonExistKeyAll(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return "JSON_CONTAINS_PATH(" + column + ", 'all', " + value + ")";
    }

}
