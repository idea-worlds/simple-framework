package dev.simpleframework.crud.method;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.method.definition.UpdateByIdDefinition;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@ModelMethod(UpdateByIdDefinition.class)
public interface UpdateById<T> {

    /**
     * 根据 id 修改模型非空字段
     *
     * @return 是否操作成功
     */
    default boolean updateById() {
        return UpdateByIdDefinition.exec(this);
    }

}
