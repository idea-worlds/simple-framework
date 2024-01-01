package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.method.definition.ListByConditionsDefinition;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(ListByConditionsDefinition.class)
public interface ListByConditions<T> {

    /**
     * 根据不为 null 的字段查询列表
     *
     * @param configs 查询配置
     * @return 列表结果
     */
    default <R extends T> List<R> listByConditions(QueryConfig configs) {
        return ListByConditionsDefinition.exec(this, configs);
    }

}
