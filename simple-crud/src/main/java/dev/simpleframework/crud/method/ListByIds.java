package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.method.definition.ListByIdsDefinition;

import java.util.Collection;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(ListByIdsDefinition.class)
public interface ListByIds<T> {

    /**
     * 根据 ids 查询
     *
     * @param ids 主键列表
     * @return 列表结果
     */
    default <R extends T> List<R> listByIds(Collection<?> ids, QueryFields... queryFields) {
        return ListByIdsDefinition.exec(this, ids, queryFields);
    }

}
