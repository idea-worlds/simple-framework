package dev.simpleframework.crud;

import dev.simpleframework.crud.core.Page;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.exception.ModelExecuteException;
import dev.simpleframework.crud.method.BaseDelete;
import dev.simpleframework.crud.method.BaseInsert;
import dev.simpleframework.crud.method.BaseQuery;
import dev.simpleframework.crud.method.BaseUpdate;
import dev.simpleframework.crud.method.definition.*;
import dev.simpleframework.crud.util.Constants;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * {@link ModelOperator} 的标准实现，通过静态工厂方法创建实例，{@code final} 类不可继承。
 * <p>
 * 提供两种创建方式：
 * <pre>{@code
 * // 绑定实体，用于 insert / updateById / updateByConditions
 * Models.wrap(user).insert();
 * Models.wrap(user).updateById();
 *
 * // 绑定类型，用于 delete / query（不需要实体字段值）
 * Models.wrap(User.class).deleteById(id);
 * Models.wrap(User.class).listByConditions(config);
 * }</pre>
 * <p>
 * 两种 wrap 均返回新实例，线程安全。
 * <p>
 * 如需自定义操作类，请实现 {@link ModelOperator} 接口并配合
 * {@code BaseInsert}、{@code BaseDelete}、{@code BaseUpdate}、{@code BaseQuery} 接口，
 * 参考本类的实现直接调用 {@code XxxDefinition.exec()} 方法。
 *
 * @param <T> POJO 实体类型
 * @author loyayz (loyayz@foxmail.com)
 * @see ModelOperator
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class Models<T> implements ModelOperator<T>, BaseInsert<T>, BaseDelete<T>, BaseUpdate<T>, BaseQuery<T> {

    private final Class<T> modelClass;
    private final T entity;

    private Models(Class<T> modelClass, T entity) {
        this.modelClass = modelClass;
        this.entity = entity;
    }

    /**
     * 绑定实体，创建可执行 insert / updateById / updateByConditions 的操作实例。
     *
     * @param entity 非空实体实例，其 class 用于查找模型信息
     */
    public static <T> Models<T> wrap(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return new Models<>((Class<T>) entity.getClass(), entity);
    }

    /**
     * 绑定类型，创建可执行 delete / query 操作的实例（不持有实体字段值）。
     *
     * @param modelClass 非空 POJO 类型
     */
    public static <T> Models<T> wrap(Class<T> modelClass) {
        Objects.requireNonNull(modelClass, "modelClass must not be null");
        return new Models<>(modelClass, null);
    }

    // ===== entity-bound =====

    @Override
    public boolean insert() {
        return InsertDefinition.exec(requireEntity());
    }

    @Override
    public boolean insertBatch(List<? extends T> models) {
        if (models == null || models.isEmpty()) {
            return false;
        }
        return InsertBatchDefinition.exec(models);
    }

    @Override
    public boolean updateById() {
        return UpdateByIdDefinition.exec(requireEntity());
    }

    @Override
    public int updateByConditions(QueryConditions conditions) {
        return UpdateByConditionsDefinition.exec(requireEntity(), conditions);
    }

    // ===== class-bound =====

    @Override
    public boolean deleteById(Object id) {
        return DeleteByIdDefinition.exec(this.modelClass, id);
    }

    @Override
    public boolean deleteByIds(Collection<?> ids) {
        return DeleteByIdsDefinition.exec(this.modelClass, ids);
    }

    @Override
    public int deleteByConditions(QueryConditions conditions) {
        return DeleteByConditionsDefinition.exec(this.modelClass, conditions);
    }

    @Override
    public <R extends T> R findById(Object id, QueryFields... queryFields) {
        return (R) FindByIdDefinition.exec(this.modelClass, id, queryFields);
    }

    @Override
    public <R extends T> R findOneByConditions(QueryConfig config) {
        List<R> list;
        if (Constants.pageHelperPresent) {
            Page<R> page = (Page<R>) (Page) PageByConditionsDefinition.exec(this.modelClass, 1, 1, false, config);
            list = page.getItems();
        } else {
            list = (List<R>) (List) ListByConditionsDefinition.exec(this.modelClass, config);
        }
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Override
    public <R extends T> List<R> listByIds(Collection<?> ids, QueryFields... queryFields) {
        return (List<R>) (List) ListByIdsDefinition.exec(this.modelClass, ids, queryFields);
    }

    @Override
    public <R extends T> List<R> listByConditions(QueryConfig config) {
        return (List<R>) (List) ListByConditionsDefinition.exec(this.modelClass, config);
    }

    @Override
    public <R extends T> Page<R> pageByConditions(int pageNum, int pageSize, QueryConfig config) {
        return (Page<R>) (Page) PageByConditionsDefinition.exec(this.modelClass, pageNum, pageSize, true, config);
    }

    @Override
    public long countByConditions(QueryConditions conditions) {
        return CountByConditionsDefinition.exec(this.modelClass, conditions);
    }

    @Override
    public boolean existByConditions(QueryConditions conditions) {
        return countByConditions(conditions) > 0;
    }

    private T requireEntity() {
        if (this.entity == null) {
            throw new ModelExecuteException(modelClass,
                    "This operation requires an entity instance. Use Models.wrap(T entity) instead of Models.wrap(Class<T>).");
        }
        return this.entity;
    }

}
