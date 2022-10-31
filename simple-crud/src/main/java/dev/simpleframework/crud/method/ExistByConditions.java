package dev.simpleframework.crud.method;

import dev.simpleframework.crud.core.QueryConditions;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface ExistByConditions<T> {

    /**
     * 根据配置查询是否存在记录
     *
     * @param conditions 查询条件
     * @return 是否存在记录
     */
    default boolean existByConditions(QueryConditions... conditions) {
        long total = MethodFindHelper.countByConditions(this, conditions);
        return total > 0;
    }

}
