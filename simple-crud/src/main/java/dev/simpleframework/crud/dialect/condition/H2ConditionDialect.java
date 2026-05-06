package dev.simpleframework.crud.dialect.condition;

import dev.simpleframework.crud.ModelField;

/**
 * H2 条件方言（适用于 H2 的 PostgreSQL 兼容模式）。
 * 数组操作符与 PG 一致，但不使用 {@code ::text[]} 类型转换。
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class H2ConditionDialect extends PgConditionDialect {
    public static final H2ConditionDialect DEFAULT = new H2ConditionDialect();

    @Override
    public String arrayContains(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        String operator = parseOperator("@>", xml);
        return column + operator + value;
    }

    @Override
    public String arrayContainedBy(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        String operator = parseOperator("<@", xml);
        return column + operator + value;
    }

    @Override
    public String arrayOverlap(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        String operator = parseOperator("&&", xml);
        return column + operator + value;
    }

}
