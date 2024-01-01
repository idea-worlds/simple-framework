package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.method.definition.CountByConditionsDefinition;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(CountByConditionsDefinition.class)
public interface CountByConditions<T> {

    /**
     * 根据配置查询统计
     *
     * @param conditions 查询条件
     * @return 统计的记录数
     */
    default long countByConditions(QueryConditions conditions) {
        return CountByConditionsDefinition.exec(this, conditions);
    }

}
