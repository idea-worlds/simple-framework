package dev.simpleframework.crud;

import dev.simpleframework.crud.method.BaseDelete;
import dev.simpleframework.crud.method.BaseInsert;
import dev.simpleframework.crud.method.BaseQuery;
import dev.simpleframework.crud.method.BaseUpdate;

/**
 * 基础模型
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface BaseModel<T> extends BaseInsert<T>, BaseDelete<T>, BaseUpdate<T>, BaseQuery<T> {

}
