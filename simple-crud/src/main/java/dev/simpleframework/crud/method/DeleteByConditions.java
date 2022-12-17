package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.method.definition.DeleteByConditionsDefinition;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(DeleteByConditionsDefinition.class)
public interface DeleteByConditions<T> {

    /**
     * 根据条件删除
     * 注意：当模型内字段值都为 null 且没有 conditions 时将执行无条件删除
     *
     * @param conditions 条件
     * @return 删除记录数
     */
    default int deleteByConditions(QueryConditions... conditions) {
        return DeleteByConditionsDefinition.exec(this, conditions);
    }

}
