package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.core.QueryConditionField;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QuerySorters;
import dev.simpleframework.crud.dialect.Dialects;
import dev.simpleframework.crud.dialect.condition.ConditionDialect;
import dev.simpleframework.crud.exception.CrudOperatorException;
import dev.simpleframework.crud.util.MybatisTypeHandler;
import dev.simpleframework.util.Strings;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MybatisScripts {

    /**
     * 转换成 if 标签的脚本片段
     *
     * @param script script
     * @return <if test="类字段名 != null">script</if>
     */
    public static String wrapperIf(ModelField<?> field, String script) {
        return wrapperIf(null, field, script);
    }

    public static String wrapperIf(String namePrefix, ModelField<?> field, String script) {
        if (field.fieldType().isPrimitive()) {
            return script;
        }
        String fieldName = Strings.isBlank(namePrefix) ?
                field.fieldName() : namePrefix + "." + field.fieldName();
        Class<?> fieldType = field.fieldType();
        if (fieldType.isArray()) {
            return String.format("<if test=\"%s != null and %s.length != 0\">%s</if>", fieldName, fieldName, script);
        }
        if (Collection.class.isAssignableFrom(fieldType)) {
            return String.format("<if test=\"%s != null and !%s.isEmpty()\">%s</if>", fieldName, fieldName, script);
        }
        return String.format("<if test=\"%s != null\">%s</if>", fieldName, script);
    }

    /**
     * 循环的脚本片段
     */
    public static String foreach(String collection, String item) {
        return String.format(
                "<foreach collection=\"%s\" item=\"%s\" open=\"(\" separator=\",\" close=\")\">#{%s}</foreach>",
                collection, item, item);
    }

    /**
     * 字段脚本
     *
     * @param fields 模型字段
     */
    public static String columnScript(List<? extends ModelField<?>> fields) {
        return fields.stream()
                .map(field -> {
                    String column = field.columnName();
                    String fieldName = field.fieldName();
                    return column.equals(fieldName) ?
                            column :
                            String.format("%s AS %s", column, fieldName);
                })
                .collect(Collectors.joining(","));
    }

    /**
     * 排序脚本
     */
    public static String sortScript(List<? extends ModelField<?>> fields, QuerySorters sorter) {
        Map<String, Boolean> sorterItems = sorter.getItems();
        if (sorterItems.isEmpty()) {
            return "";
        }
        Map<String, String> fieldColumns = fields.stream()
                .collect(Collectors.toMap(ModelField::fieldName, ModelField::columnName));
        List<String> scripts = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : sorterItems.entrySet()) {
            String column = fieldColumns.get(entry.getKey());
            if (column == null) {
                continue;
            }
            String sortType = entry.getValue() ? "asc" : "desc";
            scripts.add(String.format("%s %s", column, sortType));
        }
        if (scripts.isEmpty()) {
            return "";
        }
        return " \nORDER BY " + String.join(",", scripts);
    }

    /**
     * 条件脚本
     *
     * @param configuration mybatis 配置
     * @param fields        模型字段
     * @param conditions    条件配置
     */
    public static String conditionScript(Configuration configuration, List<? extends ModelField<?>> fields, QueryConditions conditions) {
        ConditionDialect dialect = Dialects.condition(configuration.getEnvironment().getDataSource());
        Map<String, ModelField<?>> fieldMap = fields.stream().collect(Collectors.toMap(ModelField::fieldName, f -> f));
        String script = resolveConditions(dialect, fieldMap, conditions);
        return " \n<where>\n" + script + "\n</where> ";
    }

    private static String resolveConditions(ConditionDialect dialect, Map<String, ModelField<?>> fields, QueryConditions conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String logic = QueryConditions.TYPE_OR.equals(conditions.getType()) ? "OR" : "AND";
        for (QueryConditionField condition : conditions.getFields()) {
            ModelField<?> field = fields.get(condition.getName());
            String script = resolveCondition(dialect, field, condition, logic);
            if (script.isBlank()) {
                continue;
            }
            result.append(script).append("\n");
        }
        for (QueryConditions subConditions : conditions.getSubConditions()) {
            String script = resolveConditions(dialect, fields, subConditions);
            if (script.isBlank()) {
                continue;
            }
            script = script.trim();
            if (script.startsWith("AND")) {
                script = script.substring(3);
            } else if (script.startsWith("OR")) {
                script = script.substring(2);
            }
            script = script.trim();
            while (script.startsWith("(") && script.endsWith(")")) {
                script = script.substring(1);
                script = script.substring(0, script.length() - 1);
                script = script.trim();
            }
            result.append(logic).append(" (\n").append(script).append("\n)\n");
        }
        return result.toString();
    }

    private static String resolveCondition(ConditionDialect dialect, ModelField<?> field, QueryConditionField condition, String logic) {
        if (field == null || condition == null) {
            return "";
        }
        String script;
        String fieldKey = QueryConditions.KEY_NAME + "." + condition.getKey();
        String fieldKeyParam = "#{" + fieldKey + "}";
        boolean valueMaybeNull = true;
        switch (condition.getType()) {
            case equal -> script = dialect.equal(field, fieldKeyParam, true);
            case not_equal -> script = dialect.notEqual(field, fieldKeyParam, true);
            case like_all -> script = dialect.likeAll(field, fieldKeyParam, true);
            case like_left -> script = dialect.likeLeft(field, fieldKeyParam, true);
            case like_right -> script = dialect.likeRight(field, fieldKeyParam, true);
            case greater_than -> script = dialect.greaterThan(field, fieldKeyParam, true);
            case great_equal -> script = dialect.greatEqual(field, fieldKeyParam, true);
            case less_than -> script = dialect.lessThan(field, fieldKeyParam, true);
            case less_equal -> script = dialect.lessEqual(field, fieldKeyParam, true);
            case in -> {
                String val = foreach(fieldKey, "_" + field.fieldName());
                script = dialect.in(field, val, true);
            }
            case not_in -> {
                String val = foreach(fieldKey, "_" + field.fieldName());
                script = dialect.notIn(field, val, true);
            }
            case is_null -> {
                script = dialect.isNull(field, fieldKeyParam, true);
                valueMaybeNull = false;
            }
            case not_null -> {
                script = dialect.notNull(field, fieldKeyParam, true);
                valueMaybeNull = false;
            }
            case array_contains -> {
                String val = "#{" + MybatisTypeHandler.resolveFieldName(field, fieldKey) + "}";
                script = dialect.arrayContains(field, val, true);
            }
            case array_contained_by -> {
                String val = "#{" + MybatisTypeHandler.resolveFieldName(field, fieldKey) + "}";
                script = dialect.arrayContainedBy(field, val, true);
            }
            case array_overlap -> {
                String val = "#{" + MybatisTypeHandler.resolveFieldName(field, fieldKey) + "}";
                script = dialect.arrayOverlap(field, val, true);
            }
            case json_contains -> {
                String val = "#{" + MybatisTypeHandler.resolveFieldName(Map.class, fieldKey) + "}";
                script = dialect.jsonContains(field, val, true);
            }
            case json_contained_by -> {
                String val = "#{" + MybatisTypeHandler.resolveFieldName(Map.class, fieldKey) + "}";
                script = dialect.jsonContainedBy(field, val, true);
            }
            case json_exist_key -> {
                script = dialect.jsonExistKey(field, fieldKeyParam, true);
            }
            case json_exist_key_any -> {
                String val = "#{" + MybatisTypeHandler.resolveFieldName(List.class, fieldKey) + "}";
                script = dialect.jsonExistKeyAny(field, val, true);
            }
            case json_exist_key_all -> {
                String val = "#{" + MybatisTypeHandler.resolveFieldName(List.class, fieldKey) + "}";
                script = dialect.jsonExistKeyAll(field, val, true);
            }
            default -> throw new CrudOperatorException("Not support conditionType [" + condition.getType() + "]");
        }
        script = logic + " " + script;
        return valueMaybeNull && condition.getValue() == null ? "" : script;
    }

}
