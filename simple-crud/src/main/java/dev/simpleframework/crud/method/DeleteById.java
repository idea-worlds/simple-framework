package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.method.definition.DeleteByIdDefinition;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(DeleteByIdDefinition.class)
public interface DeleteById<T> {

    /**
     * 根据 id 删除
     *
     * @param id 主键
     * @return 是否操作成功
     */
    default boolean deleteById(Object id) {
        return DeleteByIdDefinition.exec(this, id);
    }

}
