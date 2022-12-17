package dev.simpleframework.crud.method.definition;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.method.ModelMethodDefinition;
import dev.simpleframework.crud.method.impl.MybatisFindByIdMethod;
import dev.simpleframework.crud.util.ModelCache;

public class FindByIdDefinition implements ModelMethodDefinition {
    public static final String METHOD_NAME = "findById";

    @Override
    public void register(ModelInfo<?> info) {
        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            MybatisFindByIdMethod.register(info, methodId);
        } else {
            throw new ModelRegisterException(info.modelClass(), info.datasourceType());
        }
    }

    public static <T, R extends T> R exec(T model, Object id, QueryFields... queryFields) {
        if (id == null) {
            return null;
        }
        QueryFields combinedFields = QueryFields.combineFields(queryFields);
        ModelInfo<T> info = ModelCache.info(model);
        info.validIdExist();

        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisFindByIdMethod.exec(info, methodId, id, combinedFields);
        }
        return null;
    }

    private static String methodId(ModelInfo<?> info) {
        return ModelMethodDefinition.methodId(info, METHOD_NAME);
    }

}
