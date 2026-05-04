package dev.simpleframework.crud.dialect.condition;

import dev.simpleframework.crud.ModelField;

/**
 * PostgreSQL 条件方言。
 * 数组条件使用 PG 数组操作符（{@code @>} / {@code <@} / {@code &&}），
 * JSON 条件使用 jsonb 操作符（{@code @>} / {@code <@} / {@code ??} / {@code ??|} / {@code ??&}）。
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class PgConditionDialect extends SqlConditionDialect {
    public static final PgConditionDialect DEFAULT = new PgConditionDialect();

    @Override
    public String arrayContains(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("@>", xml);
        String result = column + operator + value;
        if (String.class.isAssignableFrom(field.fieldComponentType())) {
            result = result.trim() + "::text[]";
        }
        return result;
    }

    @Override
    public String arrayContainedBy(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("<@", xml);
        String result = column + operator + value;
        if (String.class.isAssignableFrom(field.fieldComponentType())) {
            result = result.trim() + "::text[]";
        }
        return result;
    }

    @Override
    public String arrayOverlap(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("&&", xml);
        String result = column + operator + value;
        if (String.class.isAssignableFrom(field.fieldComponentType())) {
            result = result.trim() + "::text[]";
        }
        return result;
    }

    @Override
    public String jsonContains(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("@>", xml);
        return column + operator + value;
    }

    @Override
    public String jsonContainedBy(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("<@", xml);
        return column + operator + value;
    }

    @Override
    public String jsonExistKey(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("??", xml);
        return column + operator + value;
    }

    @Override
    public String jsonExistKeyAny(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("??|", xml);
        return column + operator + value;
    }

    @Override
    public String jsonExistKeyAll(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("??&", xml);
        return column + operator + value;
    }

}
