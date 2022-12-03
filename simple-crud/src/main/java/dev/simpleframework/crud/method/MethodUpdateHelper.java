package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.QueryConditions;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MethodUpdateHelper {

    public static <T> boolean updateById(T model) {
        ModelInfo<T> info = Models.info(model);
        info.validIdExist();
        fillValue(info, model);

        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisUpdateHelper.updateById(info, model);
        }
        return false;
    }

    public static <T> int updateByConditions(T model, QueryConditions... conditions) {
        QueryConditions combinedConditions = QueryConditions.combineConditions(conditions);
        ModelInfo<T> info = Models.info(model);
        fillValue(info, model);

        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisUpdateHelper.updateByConditions(info, model, combinedConditions);
        }
        return 0;
    }

    private static <T> void fillValue(ModelInfo<T> info, T model) {
        for (ModelField<T> field : info.getUpdateFields()) {
            field.autoFillValue(model);
        }
    }

}
