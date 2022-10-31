package dev.simpleframework.crud.method;

import java.util.Collection;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface DeleteByIds<T> {


    /**
     * 根据 ids 批量删除
     *
     * @param ids 主键列表
     * @return 是否操作成功
     */
    default boolean deleteByIds(Collection<?> ids){
        return MethodDeleteHelper.deleteByIds(this, ids);
    }

}
