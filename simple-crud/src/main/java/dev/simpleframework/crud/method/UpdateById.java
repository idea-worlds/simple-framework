package dev.simpleframework.crud.method;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface UpdateById<T> {

    /**
     * 根据 id 修改模型非空字段
     *
     * @return 是否操作成功
     */
    default boolean updateById(){
        return MethodUpdateHelper.updateById(this);
    }

}
