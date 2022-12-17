package dev.simpleframework.crud.method.definition;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.method.ModelMethodDefinition;
import dev.simpleframework.crud.method.impl.MybatisDeleteByIdsMethod;
import dev.simpleframework.crud.util.ModelCache;

import java.util.Collection;

public class DeleteByIdsDefinition implements ModelMethodDefinition {
    public static final String METHOD_NAME = "deleteByIds";

    @Override
    public void register(ModelInfo<?> info) {
        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            MybatisDeleteByIdsMethod.register(info, methodId);
        } else {
            throw new ModelRegisterException(info.modelClass(), info.datasourceType());
        }
    }

    public static <T> boolean exec(T model, Collection<?> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        ModelInfo<T> info = ModelCache.info(model);

        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisDeleteByIdsMethod.exec(info, methodId, ids);
        }
        return false;
    }

    private static String methodId(ModelInfo<?> info) {
        return ModelMethodDefinition.methodId(info, METHOD_NAME);
    }

}
