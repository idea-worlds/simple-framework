package dev.simpleframework.crud.method.definition;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.method.ModelMethodDefinition;
import dev.simpleframework.crud.method.impl.MybatisListByConditionsMethod;
import dev.simpleframework.crud.util.ModelCache;

import java.util.List;

public class ListByConditionsDefinition implements ModelMethodDefinition {
    public static final String METHOD_NAME = "listByConditions";

    @Override
    public void register(ModelInfo<?> info) {
        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            MybatisListByConditionsMethod.register(info, methodId);
        } else {
            throw new ModelRegisterException(info.modelClass(), info.datasourceType());
        }
    }

    public static <T, R extends T> List<R> exec(T model, QueryConfig queryConfig) {
        ModelInfo<T> info = ModelCache.info(model);

        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisListByConditionsMethod.exec(info, methodId, queryConfig);
        }
        return null;
    }

    private static String methodId(ModelInfo<?> info) {
        return ModelMethodDefinition.methodId(info, METHOD_NAME);
    }

}
