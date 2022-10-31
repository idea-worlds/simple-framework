package dev.simpleframework.crud.method;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface Insert<T> {

    /**
     * 新增（非空字段）
     *
     * @return 是否操作成功
     */
    default <R extends T> boolean insert(){
        return MethodInsertHelper.insert(this);
    }

}
