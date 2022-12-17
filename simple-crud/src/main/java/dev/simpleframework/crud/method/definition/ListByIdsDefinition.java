package dev.simpleframework.crud.method.definition;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.method.ModelMethodDefinition;
import dev.simpleframework.crud.method.impl.MybatisListByIdsMethod;
import dev.simpleframework.crud.util.ModelCache;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListByIdsDefinition implements ModelMethodDefinition {
    public static final String METHOD_NAME = "listByIds";

    @Override
    public void register(ModelInfo<?> info) {
        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            MybatisListByIdsMethod.register(info, methodId);
        } else {
            throw new ModelRegisterException(info.modelClass(), info.datasourceType());
        }
    }

    public static <T, R extends T> List<R> exec(T model, Collection<?> ids, QueryFields... queryFields) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        QueryFields combinedFields = QueryFields.combineFields(queryFields);
        ModelInfo<T> info = ModelCache.info(model);
        info.validIdExist();

        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisListByIdsMethod.exec(info, methodId, ids, combinedFields);
        }
        return null;
    }

    private static String methodId(ModelInfo<?> info) {
        return ModelMethodDefinition.methodId(info, METHOD_NAME);
    }

}
