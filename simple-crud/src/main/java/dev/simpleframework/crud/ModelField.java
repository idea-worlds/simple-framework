package dev.simpleframework.crud;

/**
 * 模型字段
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface ModelField<T> {

    /**
     * 获取模型字段的值
     *
     * @param model 模型
     * @return 值
     */
    <R> R getValue(T model);

    /**
     * 设置模型字段的值
     *
     * @param model 模型
     * @param value 值
     */
    void setValue(T model, Object value);

    /**
     * 表字段名
     *
     * @return 表字段名
     */
    String columnName();

    /**
     * 类字段名
     *
     * @return 类字段名
     */
    String fieldName();

    /**
     * 类字段类型
     *
     * @return 类字段类型
     */
    Class<?> fieldType();

    /**
     * 字段是否可新增
     *
     * @return 字段是否可新增
     */
    boolean insertable();

    /**
     * 字段是否可修改
     *
     * @return 字段是否可新增
     */
    boolean updatable();

    /**
     * 字段是否可查询
     *
     * @return 是否要查询本字段
     */
    boolean selectable();

}
