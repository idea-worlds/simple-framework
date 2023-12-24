package dev.simpleframework.crud.dialect.condition;

import dev.simpleframework.crud.ModelField;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SqlConditionDialect implements ConditionDialect {
    public static final SqlConditionDialect DEFAULT = new SqlConditionDialect();

    @Override
    public String equal(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return column + " = " + value;
    }

    @Override
    public String notEqual(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return column + " != " + value;
    }

    @Override
    public String likeAll(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return column + " LIKE concat('%%'," + value + ",'%%')";
    }

    @Override
    public String likeLeft(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return column + " LIKE concat('%%'," + value + ")";
    }

    @Override
    public String likeRight(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return column + " LIKE concat(" + value + ",'%%')";
    }

    @Override
    public String greaterThan(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator(">", xml);
        return column + operator + value;
    }

    @Override
    public String greatEqual(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator(">=", xml);
        return column + operator + value;
    }

    @Override
    public String lessThan(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("<", xml);
        return column + operator + value;
    }

    @Override
    public String lessEqual(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("<=", xml);
        return column + operator + value;
    }

    @Override
    public String in(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return column + " IN " + value;
    }

    @Override
    public String notIn(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return column + " NOT IN " + value;
    }

    @Override
    public String isNull(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return column + " IS NULL";
    }

    @Override
    public String notNull(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        return column + " IS NOT NULL";
    }

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
        String operator = parseOperator("?", xml);
        return column + operator + value;
    }

    @Override
    public String jsonExistKeyAny(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("?|", xml);
        return column + operator + value;
    }

    @Override
    public String jsonExistKeyAll(ModelField<?> field, String value, boolean xml) {
        String column = field.columnName();
        String operator = parseOperator("?&", xml);
        return column + operator + value;
    }

    protected String parseOperator(String operator, boolean xml) {
        return xml ?
                " <![CDATA[ " + operator + " ]]> "
                :
                " " + operator + " ";
    }

}
