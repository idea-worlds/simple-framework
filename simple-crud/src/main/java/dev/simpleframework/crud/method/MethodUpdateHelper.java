package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.QueryConditions;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MethodUpdateHelper {

    public static <T> boolean updateById(T model) {
        ModelInfo<?> info = Models.info(model).validIdExist();
        DatasourceType datasourceType = info.config().datasourceType();

        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisUpdateHelper.updateById(info, model);
        }
        return false;
    }

    public static <T> int updateByConditions(T model, QueryConditions... conditions) {
        QueryConditions combinedConditions = QueryConditions.combineConditions(conditions);
        ModelInfo<?> info = Models.info(model);
        DatasourceType datasourceType = info.config().datasourceType();

        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisUpdateHelper.updateByConditions(info, model, combinedConditions);
        }
        return 0;
    }

}
