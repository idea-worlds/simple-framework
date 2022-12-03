package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.core.QueryFields;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MethodFindHelper {

    public static <T, R extends T> R findById(T model, Object id, QueryFields... queryFields) {
        if (id == null) {
            return null;
        }
        boolean dynamic = queryFields != null && queryFields.length > 0;
        QueryFields combinedFields = QueryFields.combineFields(queryFields);
        ModelInfo<?> info = Models.info(model).validIdExist();
        DatasourceType datasourceType = info.datasourceType();

        if (datasourceType == DatasourceType.Mybatis) {
            return dynamic ?
                    MybatisFindHelper.findByIdDynamic(info, id, combinedFields) :
                    MybatisFindHelper.findById(info, id);
        }
        return null;
    }

    public static <T> long countByConditions(T model, QueryConditions... conditions) {
        boolean dynamic = conditions != null && conditions.length > 0;
        QueryConditions combinedConditions = QueryConditions.combineConditions(conditions);
        ModelInfo<?> info = Models.info(model);
        DatasourceType datasourceType = info.datasourceType();

        if (datasourceType == DatasourceType.Mybatis) {
            return dynamic ?
                    MybatisFindHelper.countByConditionsDynamic(info, model, combinedConditions) :
                    MybatisFindHelper.countByConditions(info, model);
        }
        return 0;
    }

    public static <T, R extends T> List<R> listByIds(T model, Collection<?> ids, QueryFields... queryFields) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        boolean dynamic = queryFields != null && queryFields.length > 0;
        QueryFields combinedFields = QueryFields.combineFields(queryFields);
        ModelInfo<?> info = Models.info(model).validIdExist();
        DatasourceType datasourceType = info.datasourceType();

        if (datasourceType == DatasourceType.Mybatis) {
            return dynamic ?
                    MybatisFindHelper.listByIdsDynamic(info, ids, combinedFields) :
                    MybatisFindHelper.listByIds(info, ids);
        }
        return Collections.emptyList();
    }

    public static <T, R extends T> List<R> listByConditions(T model, QueryConfig... configs) {
        QueryConfig queryConfig = QueryConfig.combineConfigs(configs);
        boolean dynamic = configs != null && configs.length > 0;
        ModelInfo<?> info = Models.info(model);
        DatasourceType datasourceType = info.datasourceType();

        if (datasourceType == DatasourceType.Mybatis) {
            return dynamic ?
                    MybatisFindHelper.listByConditionsDynamic(info, model, queryConfig) :
                    MybatisFindHelper.listByConditions(info, model);
        }
        return Collections.emptyList();
    }

}
