package dev.simpleframework.crud.dialect.condition;

import dev.simpleframework.crud.ModelField;

/**
 * 数据库条件方言
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface ConditionDialect {

    /**
     * 等于
     */
    String equal(ModelField<?> field, String value, boolean xml);

    /**
     * 不等于
     */
    String notEqual(ModelField<?> field, String value, boolean xml);

    /**
     * 全模糊
     */
    String likeAll(ModelField<?> field, String value, boolean xml);

    /**
     * 左模糊
     */
    String likeLeft(ModelField<?> field, String value, boolean xml);

    /**
     * 右模糊
     */
    String likeRight(ModelField<?> field, String value, boolean xml);

    /**
     * 大于
     */
    String greaterThan(ModelField<?> field, String value, boolean xml);

    /**
     * 大于等于
     */
    String greatEqual(ModelField<?> field, String value, boolean xml);

    /**
     * 小于
     */
    String lessThan(ModelField<?> field, String value, boolean xml);

    /**
     * 小于等于
     */
    String lessEqual(ModelField<?> field, String value, boolean xml);

    /**
     * 包含
     */
    String in(ModelField<?> field, String value, boolean xml);

    /**
     * 不包含
     */
    String notIn(ModelField<?> field, String value, boolean xml);

    /**
     * 为 null
     */
    String isNull(ModelField<?> field, String value, boolean xml);

    /**
     * 不为 null
     */
    String notNull(ModelField<?> field, String value, boolean xml);

    /**
     * 数组：包含
     */
    String arrayContains(ModelField<?> field, String value, boolean xml);

    /**
     * 数组：被包含
     */
    String arrayContainedBy(ModelField<?> field, String value, boolean xml);

    /**
     * 数组：相交
     */
    String arrayOverlap(ModelField<?> field, String value, boolean xml);

    /**
     * json 包含对象
     */
    String jsonContains(ModelField<?> field, String value, boolean xml);

    /**
     * json 被对象包含
     */
    String jsonContainedBy(ModelField<?> field, String value, boolean xml);

    /**
     * json 有指定 key
     */
    String jsonExistKey(ModelField<?> field, String value, boolean xml);

    /**
     * json 有任一 key
     */
    String jsonExistKeyAny(ModelField<?> field, String value, boolean xml);

    /**
     * json 有所有 key
     */
    String jsonExistKeyAll(ModelField<?> field, String value, boolean xml);

    /**
     * 引用标识符（列别名等），默认双引号（SQL 标准）
     */
    default String quot(String identifier) {
        return "\"" + identifier + "\"";
    }

    /**
     * 条件/别名中的列名引用。根据 {@code Dialects.isQuoteColumnNames()} 决定是否用 quot() 包裹。
     * 默认 false（保持现有行为）。开启后解决 SQL 关键字冲突，需保证 @Column(name) 与 DB 实际列名一致。
     */
    default String column(ModelField<?> field) {
        String name = field.columnName();
        return dev.simpleframework.crud.dialect.Dialects.isQuoteColumnNames() ? quot(name) : name;
    }

}
