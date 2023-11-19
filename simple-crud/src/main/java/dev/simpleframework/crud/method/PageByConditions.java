package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.core.Page;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.method.definition.PageByConditionsDefinition;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(PageByConditionsDefinition.class)
public interface PageByConditions<T> {

    /**
     * 根据不为 null 的字段查询分页
     *
     * @param pageNum  第几页，从 1 开始
     * @param pageSize 每页数量
     * @param configs  查询配置
     * @return 分页结果
     */
    default <R extends T> Page<R> pageByConditions(int pageNum, int pageSize, QueryConfig... configs) {
        return PageByConditionsDefinition.exec(this, pageNum, pageSize, true, configs);
    }

}
