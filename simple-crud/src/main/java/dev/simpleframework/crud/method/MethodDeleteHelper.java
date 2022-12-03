package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.QueryConditions;

import java.util.Collection;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MethodDeleteHelper {

    public static <T> boolean deleteById(T model, Object id) {
        if (id == null) {
            return false;
        }
        ModelInfo<?> info = Models.info(model).validIdExist();
        DatasourceType datasourceType = info.datasourceType();

        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisDeleteHelper.deleteById(info, id);
        }
        return false;
    }

    public static <T> boolean deleteByIds(T model, Collection<?> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        ModelInfo<?> info = Models.info(model).validIdExist();
        DatasourceType datasourceType = info.datasourceType();

        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisDeleteHelper.deleteByIds(info, ids);
        }
        return false;
    }

    public static <T> int deleteByConditions(T model, QueryConditions... conditions) {
        boolean dynamic = conditions != null && conditions.length > 0;
        QueryConditions combinedConditions = QueryConditions.combineConditions(conditions);
        ModelInfo<?> info = Models.info(model);
        DatasourceType datasourceType = info.datasourceType();

        if (datasourceType == DatasourceType.Mybatis) {
            return dynamic ?
                    MybatisDeleteHelper.deleteByConditionsDynamic(info, model, combinedConditions) :
                    MybatisDeleteHelper.deleteByConditions(info, model);
        }
        return 0;
    }

}
