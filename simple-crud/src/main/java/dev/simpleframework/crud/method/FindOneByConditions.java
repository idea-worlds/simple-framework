package dev.simpleframework.crud.method;

import dev.simpleframework.crud.core.Page;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.exception.SimpleCrudException;
import dev.simpleframework.crud.method.definition.ListByConditionsDefinition;
import dev.simpleframework.crud.method.definition.PageByConditionsDefinition;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface FindOneByConditions<T> {

    /**
     * 根据不为 null 的字段获取单个数据，多条数据时取第一个
     *
     * @param configs 查询配置
     * @return 查询结果
     */
    default <R extends T> R findOneByConditions(QueryConfig... configs) {
        List<R> list;
        try {
            Page<R> page = PageByConditionsDefinition.exec(this, 1, 1, false, configs);
            list = page.getItems();
        } catch (SimpleCrudException e) {
            list = ListByConditionsDefinition.exec(this, configs);
        }
        return list == null ? null : list.get(0);
    }

}
