package dev.simpleframework.crud.dialect.condition;

import dev.simpleframework.crud.ModelField;

/**
 * Oracle 条件方言。
 * <p>
 * LIKE 使用 {@code ||} 字符串拼接（Oracle {@code CONCAT} 仅支持两个参数），
 * 数组/JSON 条件通过 {@code JSON_EXISTS} 函数实现（适用 Oracle 12c+）。
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class OracleConditionDialect extends SqlConditionDialect {
    public static final OracleConditionDialect DEFAULT = new OracleConditionDialect();

    // ──── LIKE → || ────

    @Override
    public String likeAll(ModelField<?> field, String value, boolean xml) {
        return column(field) + " LIKE '%' || " + value + " || '%'";
    }

    @Override
    public String likeLeft(ModelField<?> field, String value, boolean xml) {
        return column(field) + " LIKE '%' || " + value;
    }

    @Override
    public String likeRight(ModelField<?> field, String value, boolean xml) {
        return column(field) + " LIKE " + value + " || '%'";
    }

    // ──── 数组 → JSON_EXISTS ────

    @Override
    public String arrayContains(ModelField<?> field, String value, boolean xml) {
        return "JSON_EXISTS(" + column(field) + ", " + value + ")";
    }

    @Override
    public String arrayContainedBy(ModelField<?> field, String value, boolean xml) {
        return "JSON_EXISTS(" + value + ", " + column(field) + ")";
    }

    @Override
    public String arrayOverlap(ModelField<?> field, String value, boolean xml) {
        return "JSON_EXISTS(" + column(field) + ", " + value + ")";
    }

    // ──── JSON → JSON_EXISTS ────

    @Override
    public String jsonContains(ModelField<?> field, String value, boolean xml) {
        return "JSON_EXISTS(" + column(field) + ", " + value + ")";
    }

    @Override
    public String jsonContainedBy(ModelField<?> field, String value, boolean xml) {
        return "JSON_EXISTS(" + value + ", " + column(field) + ")";
    }

    @Override
    public String jsonExistKey(ModelField<?> field, String value, boolean xml) {
        return "JSON_EXISTS(" + column(field) + ", " + value + ")";
    }

    @Override
    public String jsonExistKeyAny(ModelField<?> field, String value, boolean xml) {
        return "JSON_EXISTS(" + column(field) + ", " + value + ")";
    }

    @Override
    public String jsonExistKeyAll(ModelField<?> field, String value, boolean xml) {
        return "JSON_EXISTS(" + column(field) + ", " + value + ")";
    }

}
