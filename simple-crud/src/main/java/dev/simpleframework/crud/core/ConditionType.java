package dev.simpleframework.crud.core;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public enum ConditionType {

    /**
     * 等于
     */
    equal,
    /**
     * 不等于
     */
    not_equal,
    /**
     * 全模糊
     */
    like_all,
    /**
     * 左模糊
     */
    like_left,
    /**
     * 右模糊
     */
    like_right,
    /**
     * 大于
     */
    greater_than,
    /**
     * 大于等于
     */
    great_equal,
    /**
     * 小于
     */
    less_than,
    /**
     * 小于等于
     */
    less_equal,
    /**
     * 包含
     */
    in,
    /**
     * 不包含
     */
    not_in,
    /**
     * 为 null
     */
    is_null,
    /**
     * 不为 null
     */
    not_null,
    /**
     * 数组：包含
     */
    array_contains,
    /**
     * 数组：被包含
     */
    array_contained_by,
    /**
     * 数组：相交
     */
    array_overlap,
    /**
     * json 包含对象
     */
    json_contains,
    /**
     * json 被对象包含
     */
    json_contained_by,
    /**
     * json 有指定 key
     */
    json_exist_key,
    /**
     * json 有任一 key
     */
    json_exist_key_any,
    /**
     * json 有所有 key
     */
    json_exist_key_all,

}
