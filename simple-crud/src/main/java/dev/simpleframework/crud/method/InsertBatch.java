package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.method.definition.InsertBatchDefinition;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(InsertBatchDefinition.class)
public interface InsertBatch<T> {

    /**
     * 批量新增（所有字段）
     *
     * @param models 要新增的模型列表
     * @return 是否操作成功
     */
    default boolean insertBatch(List<? extends T> models) {
        return InsertBatchDefinition.exec(models);
    }

}
