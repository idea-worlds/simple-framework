package dev.simpleframework.crud.method;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface BaseQuery<T> extends
        FindById<T>,
        ListByIds<T>,
        ListByConditions<T>,
        PageByConditions<T>,
        CountByConditions<T>,
        ExistByConditions<T> {
}
