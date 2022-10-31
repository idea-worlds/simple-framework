package dev.simpleframework.crud.method;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface DeleteById<T> {

    /**
     * 根据 id 删除
     *
     * @param id 主键
     * @return 是否操作成功
     */
    default boolean deleteById(Object id) {
        return MethodDeleteHelper.deleteById(this, id);
    }

}
