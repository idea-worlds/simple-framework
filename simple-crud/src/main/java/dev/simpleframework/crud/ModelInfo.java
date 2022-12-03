package dev.simpleframework.crud;

import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.exception.FieldDefinitionException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型信息
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface ModelInfo<T> {

    /**
     * 模型类
     *
     * @return 当前模型信息注册时的模型类
     */
    Class<T> modelClass();

    /**
     * 模型名
     *
     * @return 模型名/表名
     */
    String name();

    /**
     * 数据源类型
     *
     * @return 数据源类型
     */
    DatasourceType datasourceType();

    /**
     * 数据源名称
     *
     * @return 数据源名称
     */
    String datasourceName();

    /**
     * 模型 id 字段
     *
     * @return 模型 id 字段，可为空
     */
    ModelField<T> id();

    /**
     * 校验模型 id 字段是否存在，不存在时抛异常
     */
    default ModelInfo<T> validIdExist() {
        if (this.id() == null) {
            throw new FieldDefinitionException(this.name(), "id field is not defined");
        }
        return this;
    }

    /**
     * 所有字段列表
     *
     * @return 模型中的所有字段
     */
    List<ModelField<T>> getAllFields();

    /**
     * 可新增的字段列表
     *
     * @return getAllFields() 里 insertable = true 的字段
     */
    default List<ModelField<T>> getInsertFields() {
        return this.getAllFields()
                .stream()
                .filter(ModelField::insertable)
                .collect(Collectors.toList());
    }

    /**
     * 可修改的字段列表
     *
     * @return getAllFields() 里 updatable = true 的字段
     */
    default List<ModelField<T>> getUpdateFields() {
        return this.getAllFields()
                .stream()
                .filter(ModelField::updatable)
                .collect(Collectors.toList());
    }

    /**
     * 可查询的字段列表
     *
     * @return getAllFields() 里 selectable = true 的字段
     */
    default List<ModelField<T>> getSelectFields() {
        return this.getAllFields()
                .stream()
                .filter(ModelField::selectable)
                .collect(Collectors.toList());
    }

    /**
     * 根据字段名获取字段
     *
     * @param fieldName 类字段名
     * @return 字段
     */
    ModelField<T> getField(String fieldName);

    /**
     * 增删改查方法名命名空间
     */
    default String methodNamespace() {
        return this.modelClass().getName();
    }

    /**
     * 是否动态模型
     */
    default boolean dynamic() {
        return false;
    }

}
