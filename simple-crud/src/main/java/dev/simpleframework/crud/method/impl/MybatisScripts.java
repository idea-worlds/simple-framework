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
     * 条件脚本：生成 MyBatis {@code <where>} 动态 SQL 片段。
     * 先根据 JDBC URL 选取对应数据库方言，再递归解析所有条件（含嵌套子条件）。
     *
     * @param configuration mybatis 配置（用于获取 DataSource 和 ScriptingLanguage）
     * @param fields        模型字段，用于将字段名映射到列名及类型处理器
     * @param conditions    树形条件配置（支持 AND/OR 组合及嵌套）
     * @return 形如 {@code <where>AND col = #{data.field}</where>} 的 XML 脚本片段
     */
    public static String conditionScript(Configuration configuration, List<? extends ModelField<?>> fields, QueryConditions conditions) {
        ConditionDialect dialect = Dialects.condition(configuration.getEnvironment().getDataSource());
        Map<String, ModelField<?>> fieldMap = fields.stream().collect(Collectors.toMap(ModelField::fieldName, f -> f));
        String script = resolveConditions(dialect, fieldMap, conditions);
        return " \n<where>\n" + script + "\n</where> ";
    }

    /**
     * 递归解析条件树，生成 AND/OR 组合的 SQL 片段。
     * <p>
     * 处理流程：
     * <ol>
     *   <li>遍历当前层的叶子条件，逐条生成带逻辑前缀（AND/OR）的 SQL 片段；</li>
     *   <li>递归解析每个子 {@link QueryConditions}，剥除其最外层的逻辑关键字和括号后，
     *       用 {@code logic (子条件)} 包裹追加。</li>
     * </ol>
     * 注意：剥除子条件首尾多余括号是为了防止 {@code ((...))} 双重包裹。
     */
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
            // 剥除子脚本首部的 AND/OR 前缀，再统一用当前层的 logic 前缀包裹
            script = script.trim();
            if (script.startsWith("AND")) {
                script = script.substring(3);
            } else if (script.startsWith("OR")) {
                script = script.substring(2);
            }
            script = script.trim();
            // 剥除多余括号，避免嵌套出现 ((子条件))
            while (script.startsWith("(") && script.endsWith(")")) {
                script = script.substring(1);
                script = script.substring(0, script.length() - 1);
                script = script.trim();
            }
            result.append(logic).append(" (\n").append(script).append("\n)\n");
        }
        return result.toString();
    }

    /**
     * 将单个条件字段转换为带逻辑前缀的 MyBatis XML SQL 片段。
     * <p>
     * 参数绑定规则：
     * <ul>
     *   <li>普通条件：{@code #{data.fieldKey}}，值为 null 时返回空串（跳过该条件）；</li>
     *   <li>{@code is_null}/{@code not_null}：不需要绑定值，始终输出；</li>
     *   <li>数组/JSON 类型：通过 {@link MybatisTypeHandler} 附加 TypeHandler 信息。</li>
     * </ul>
     */
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
            case greater_equal -> script = dialect.greatEqual(field, fieldKeyParam, true);
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
