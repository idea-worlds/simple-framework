package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.method.definition.InsertDefinition;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(InsertDefinition.class)
public interface Insert<T> {

    /**
     * 新增（非空字段）
     *
     * @return 是否操作成功
     */
    default <R extends T> boolean insert() {
        return InsertDefinition.exec(this);
    }

}
