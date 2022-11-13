package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.core.ConditionType;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QuerySorters;
import dev.simpleframework.crud.exception.SimpleCrudException;
import dev.simpleframework.crud.util.MybatisTypeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
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
        if (field.fieldType().isPrimitive()) {
            return script;
        }
        return wrapperIf(field.fieldName(), script);
    }

    /**
     * 转换成 if 标签的脚本片段
     *
     * @param script script
     * @return <if test="类字段名 != null">script</if>
     */
    public static String wrapperIf(String fieldName, String script) {
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
        QueryConditions.ConditionInfo defaultCondition = QueryConditions.ConditionInfo.of(ConditionType.equal, null);
        String script = fields.stream()
                .map(field -> CONDITION_SCRIPT_PROVIDER.apply(field, defaultCondition))
                .collect(Collectors.joining("\n"));
        return " \n<where>\n" + script + "\n</where> ";
    }

    /**
     * 条件脚本
     *
     * @param fields     模型字段
     * @param conditions 条件配置
     */
    public static String conditionScript(List<? extends ModelField<?>> fields, QueryConditions conditions) {
        QueryConditions.ConditionInfo defaultCondition = QueryConditions.ConditionInfo.of(ConditionType.equal, null);
        String script = fields.stream()
                .flatMap(field -> {
                            List<String> fieldScripts = new ArrayList<>();
                            List<QueryConditions.ConditionInfo> fieldConditions = conditions.getConditionInfos(field.fieldName());
                            if (fieldConditions.isEmpty()) {
                                fieldScripts.add(CONDITION_SCRIPT_PROVIDER.apply(field, defaultCondition));
                            } else {
                                for (QueryConditions.ConditionInfo fieldCondition : fieldConditions) {
                                    fieldScripts.add(CONDITION_SCRIPT_PROVIDER.apply(field, fieldCondition));
                                }
                            }
                            return fieldScripts.stream();
                        }
                )
                .collect(Collectors.joining("\n"));
        return " \n<where>\n" + script + "\n</where> ";
    }

    private static final BiFunction<ModelField<?>, QueryConditions.ConditionInfo, String> CONDITION_SCRIPT_PROVIDER =
            (field, condition) -> {
                String script;
                String column = field.columnName();
                String fieldName = field.fieldName();
                String fieldKey;
                if (condition.getValue() == null) {
                    fieldKey = String.format("%s.%s", "model", fieldName);
                } else {
                    fieldKey = String.format("%s.%s", "data", condition.getKey(fieldName));
                }
                String fieldParam = MybatisTypeHandler.resolveFieldName(field, fieldKey);
                boolean needWrapIf = true;
                switch (condition.getType()) {
                    case equal:
                        script = String.format("%s = #{%s}", column, fieldParam);
                        break;
                    case not_equal:
                        script = String.format("%s <![CDATA[ <> ]]> #{%s}", column, fieldParam);
                        break;
                    case like_all:
                        script = String.format("%s LIKE concat('%%', #{%s}, '%%')", column, fieldParam);
                        break;
                    case like_left:
                        script = String.format("%s LIKE concat('%%', #{%s})", column, fieldParam);
                        break;
                    case like_right:
                        script = String.format("%s LIKE concat(#{%s}, '%%')", column, fieldParam);
                        break;
                    case greater_than:
                        script = String.format("%s <![CDATA[ > ]]> #{%s}", column, fieldParam);
                        break;
                    case great_equal:
                        script = String.format("%s <![CDATA[ >= ]]> #{%s}", column, fieldParam);
                        break;
                    case less_than:
                        script = String.format("%s <![CDATA[ < ]]> #{%s}", column, fieldParam);
                        break;
                    case less_equal:
                        script = String.format("%s <![CDATA[ <= ]]> #{%s}", column, fieldParam);
                        break;
                    case is_null:
                        script = String.format("%s IS NULL", column);
                        needWrapIf = false;
                        break;
                    case not_null:
                        script = String.format("%s IS NOT NULL", column);
                        needWrapIf = false;
                        break;
                    case in:
                        script = String.format("%s IN %s", column, foreach(fieldKey, "_" + fieldName, field));
                        break;
                    case not_in:
                        script = String.format("%s NOT IN %s", column, foreach(fieldKey, "_" + fieldName, field));
                        break;
                    case array_contains:
                        script = String.format("%s <![CDATA[ @> ]]> #{%s}", column, fieldParam);
                        break;
                    case array_contained_by:
                        script = String.format("%s <![CDATA[ <@ ]]> #{%s}", column, fieldParam);
                        break;
                    case array_overlap:
                        script = String.format("%s <![CDATA[ && ]]> #{%s}", column, fieldParam);
                        break;
                    default:
                        throw new SimpleCrudException("Not support conditionType [" + condition.getType() + "]");
                }
                script = " AND " + script + " ";
                if (field.fieldType().isPrimitive()) {
                    return script;
                }
                return needWrapIf ? wrapperIf(fieldKey, script) : script;
            };

}
