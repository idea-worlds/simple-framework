package dev.simpleframework.crud.method;

import dev.simpleframework.crud.core.QueryConditions;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface UpdateByConditions<T> {

    /**
     * 根据条件修改模型非空字段
     * 注意：当没有 conditions 时将执行无条件修改
     *
     * @param conditions 条件
     * @return 修改记录数
     */
    default int updateByConditions(QueryConditions... conditions) {
        return MethodUpdateHelper.updateByConditions(this, conditions);
    }

}
