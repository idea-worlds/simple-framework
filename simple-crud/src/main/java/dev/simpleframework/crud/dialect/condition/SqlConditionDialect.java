package dev.simpleframework.crud.dialect.condition;

import dev.simpleframework.crud.ModelField;

/**
 * SQL 条件方言抽象基类，提供标准 SQL 方法实现。
 * 数组和 JSON 条件由数据库特定子类实现。
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class SqlConditionDialect implements ConditionDialect {

    @Override
    public String equal(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        return column + " = " + value;
    }

    @Override
    public String notEqual(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        return column + " != " + value;
    }

    @Override
    public String likeAll(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        return column + " LIKE concat('%%'," + value + ",'%%')";
    }

    @Override
    public String likeLeft(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        return column + " LIKE concat('%%'," + value + ")";
    }

    @Override
    public String likeRight(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        return column + " LIKE concat(" + value + ",'%%')";
    }

    @Override
    public String greaterThan(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        String operator = parseOperator(">", xml);
        return column + operator + value;
    }

    @Override
    public String greatEqual(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        String operator = parseOperator(">=", xml);
        return column + operator + value;
    }

    @Override
    public String lessThan(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        String operator = parseOperator("<", xml);
        return column + operator + value;
    }

    @Override
    public String lessEqual(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        String operator = parseOperator("<=", xml);
        return column + operator + value;
    }

    @Override
    public String in(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        return column + " IN " + value;
    }

    @Override
    public String notIn(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        return column + " NOT IN " + value;
    }

    @Override
    public String isNull(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        return column + " IS NULL";
    }

    @Override
    public String notNull(ModelField<?> field, String value, boolean xml) {
        String column = column(field);
        return column + " IS NOT NULL";
    }

    protected static String parseOperator(String operator, boolean xml) {
        return xml ?
                " <![CDATA[ " + operator + " ]]> "
                :
                " " + operator + " ";
    }

}
