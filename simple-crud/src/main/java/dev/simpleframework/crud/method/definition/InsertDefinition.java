package dev.simpleframework.crud.method.definition;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.method.ModelMethodDefinition;
import dev.simpleframework.crud.method.impl.MybatisInsertMethod;
import dev.simpleframework.crud.util.ModelCache;

public class InsertDefinition implements ModelMethodDefinition {
    public static final String METHOD_NAME = "insert";

    @Override
    public void register(ModelInfo<?> info) {
        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            MybatisInsertMethod.register(info, methodId);
        } else {
            throw new ModelRegisterException(info.modelClass(), info.datasourceType());
        }
    }

    public static <T> boolean exec(T model) {
        if (model == null) {
            return false;
        }
        ModelInfo<T> info = ModelCache.info(model);
        fillValue(info, model);

        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisInsertMethod.exec(info, methodId, model);
        }
        return false;
    }

    private static <T> void fillValue(ModelInfo<T> info, T model) {
        for (ModelField<T> field : info.getInsertFields()) {
            field.autoFillValue(model);
        }
    }

    private static String methodId(ModelInfo<?> info) {
        return ModelMethodDefinition.methodId(info, METHOD_NAME);
    }

}
