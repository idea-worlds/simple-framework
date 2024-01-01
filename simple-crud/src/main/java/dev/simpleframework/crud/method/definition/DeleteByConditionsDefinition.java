package dev.simpleframework.crud.method.definition;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.method.ModelMethodDefinition;
import dev.simpleframework.crud.method.impl.MybatisDeleteByConditionsMethod;
import dev.simpleframework.crud.util.ModelCache;

public class DeleteByConditionsDefinition implements ModelMethodDefinition {
    public static final String METHOD_NAME = "deleteByConditions";

    @Override
    public void register(ModelInfo<?> info) {
        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            MybatisDeleteByConditionsMethod.register(info, methodId);
        } else {
            throw new ModelRegisterException(info.modelClass(), info.datasourceType());
        }
    }

    public static <T> int exec(T model, QueryConditions conditions) {
        ModelInfo<T> info = ModelCache.info(model);

        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisDeleteByConditionsMethod.exec(info, methodId, conditions);
        }
        return 0;
    }

    private static String methodId(ModelInfo<?> info) {
        return ModelMethodDefinition.methodId(info, METHOD_NAME);
    }

}
