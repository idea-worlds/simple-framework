package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.core.ConditionType;
import dev.simpleframework.crud.core.QueryConditionField;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QuerySorters;
import dev.simpleframework.crud.exception.ModelExecuteException;
import dev.simpleframework.crud.util.MybatisTypeHandler;
import dev.simpleframework.util.Strings;

import java.util.*;
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
        return foreach(collection, item, null);
    }

    /**
     * 循环的脚本片段
     */
    public static String foreach(String collection, String item, ModelField<?> field) {
        String itemValue = field == null ? item : MybatisTypeHandler.resolveFieldName(field, item);
        return String.format(
                "<foreach collection=\"%s\" item=\"%s\" open=\"(\" separator=\",\" close=\")\">#{%s}</foreach>",
                collection, item, itemValue);
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
     * @param fields 模型字段
     */
    public static String conditionScript(List<? extends ModelField<?>> fields) {
        return conditionScript(fields, null);
    }

    /**
     * 条件脚本
     *
     * @param fields     模型字段
     * @param conditions 条件配置
     */
    public static String conditionScript(List<? extends ModelField<?>> fields, QueryConditions conditions) {
        Map<String, ModelField<?>> fieldMap = new HashMap<>(16);
        QueryConditions actualConditions = QueryConditions.and();
        for (ModelField<?> field : fields) {
            String fieldName = field.fieldName();
            Class<?> fieldType = field.fieldType();
            ConditionType conditionType = fieldType.isArray() || Collection.class.isAssignableFrom(fieldType) ?
                    ConditionType.array_contains : ConditionType.equal;
            actualConditions.add(fieldName, conditionType, (Object) null);
            fieldMap.put(fieldName, field);
        }
        actualConditions.add(conditions, false);

        String script = resolveConditions(fieldMap, actualConditions, "model");
        return " \n<where>\n" + script + "\n</where> ";
    }

    private static String resolveConditions(Map<String, ModelField<?>> fields, QueryConditions conditions, String owner) {
        if (conditions == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String logic = QueryConditions.TYPE_OR.equals(conditions.getType()) ? "OR" : "AND";
        for (QueryConditionField condition : conditions.getFields()) {
            ModelField<?> field = fields.get(condition.getName());
            String script = resolveCondition(field, condition, owner, logic);
            if (script.isBlank()) {
                continue;
            }
            result.append(script).append("\n");
        }
        for (QueryConditions subConditions : conditions.getSubConditions()) {
            String script = resolveConditions(fields, subConditions, "data");
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

    public static String resolveCondition(ModelField<?> field, QueryConditionField condition, String owner, String logic) {
        if (field == null || condition == null) {
            return "";
        }
        String script;
        String column = field.columnName();
        String fieldName = field.fieldName();
        String fieldNameKey = owner + "." + condition.getKey();
        String fieldNameParam = MybatisTypeHandler.resolveFieldName(field, fieldNameKey);
        boolean needWrapIfNull = true;
        switch (condition.getType()) {
            case equal:
                script = String.format("%s = #{%s}", column, fieldNameParam);
                break;
            case not_equal:
                script = String.format("%s <![CDATA[ <> ]]> #{%s}", column, fieldNameParam);
                break;
            case like_all:
                script = String.format("%s LIKE concat('%%', #{%s}, '%%')", column, fieldNameParam);
                break;
            case like_left:
                script = String.format("%s LIKE concat('%%', #{%s})", column, fieldNameParam);
                break;
            case like_right:
                script = String.format("%s LIKE concat(#{%s}, '%%')", column, fieldNameParam);
                break;
            case greater_than:
                script = String.format("%s <![CDATA[ > ]]> #{%s}", column, fieldNameParam);
                break;
            case great_equal:
                script = String.format("%s <![CDATA[ >= ]]> #{%s}", column, fieldNameParam);
                break;
            case less_than:
                script = String.format("%s <![CDATA[ < ]]> #{%s}", column, fieldNameParam);
                break;
            case less_equal:
                script = String.format("%s <![CDATA[ <= ]]> #{%s}", column, fieldNameParam);
                break;
            case is_null:
                script = String.format("%s IS NULL", column);
                needWrapIfNull = false;
                break;
            case not_null:
                script = String.format("%s IS NOT NULL", column);
                needWrapIfNull = false;
                break;
            case in:
                script = String.format("%s IN %s", column, foreach(fieldNameKey, "_" + fieldName, field));
                break;
            case not_in:
                script = String.format("%s NOT IN %s", column, foreach(fieldNameKey, "_" + fieldName, field));
                break;
            case array_contains:
                script = String.format("%s <![CDATA[ @> ]]> #{%s}", column, fieldNameParam);
                if (String.class.isAssignableFrom(field.fieldComponentType())) {
                    script = script + "::text[]";
                }
                break;
            case array_contained_by:
                script = String.format("%s <![CDATA[ <@ ]]> #{%s}", column, fieldNameParam);
                if (String.class.isAssignableFrom(field.fieldComponentType())) {
                    script = script + "::text[]";
                }
                break;
            case array_overlap:
                script = String.format("%s <![CDATA[ && ]]> #{%s}", column, fieldNameParam);
                if (String.class.isAssignableFrom(field.fieldComponentType())) {
                    script = script + "::text[]";
                }
                break;
            default:
                throw new ModelExecuteException("Not support conditionType [" + condition.getType() + "]");
        }
        script = logic + " " + script;
        if ("model".equals(owner)) {
            return needWrapIfNull ? wrapperIf(owner, field, script) : script;
        } else {
            return needWrapIfNull && condition.getValue() == null ? "" : script;
        }
    }

}
