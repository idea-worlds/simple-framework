package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.method.definition.FindByIdDefinition;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(FindByIdDefinition.class)
public interface FindById<T> {

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 查询结果
     */
    @SuppressWarnings("unchecked")
    default <R extends T> R findById(Object id, QueryFields... queryFields) {
        return FindByIdDefinition.exec(this, id, queryFields);
    }

}
