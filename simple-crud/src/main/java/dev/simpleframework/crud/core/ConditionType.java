package dev.simpleframework.crud.core;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public enum ConditionType {

    /**
     * 等于，不等于
     */
    equal, not_equal,
    /**
     * 全模糊，左模糊，右模糊
     */
    like_all, like_left, like_right,
    /**
     * 大于，大于等于
     */
    greater_than, great_equal,
    /**
     * 小于，小于等于
     */
    less_than, less_equal,
    /**
     * 为 null，不为 null
     */
    is_null, not_null,
    /**
     * 包含，不包含
     */
    in, not_in,
    /**
     * 包含，被包含，相交
     */
    array_contains, array_contained_by, array_overlap

}
