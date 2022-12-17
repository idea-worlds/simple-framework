package dev.simpleframework.crud.method.definition;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.method.ModelMethodDefinition;
import dev.simpleframework.crud.method.impl.MybatisInsertBatchMethod;
import dev.simpleframework.crud.util.ModelCache;

import java.util.List;

public class InsertBatchDefinition implements ModelMethodDefinition {
    public static final String METHOD_NAME = "insertBatch";

    @Override
    public void register(ModelInfo<?> info) {
        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            MybatisInsertBatchMethod.register(info, methodId);
        } else {
            throw new ModelRegisterException(info.modelClass(), info.datasourceType());
        }
    }

    public static <T> boolean exec(List<? extends T> models) {
        if (models.isEmpty()) {
            return false;
        }
        ModelInfo<T> info = ModelCache.info(models.get(0));
        fillValue(info, models);

        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisInsertBatchMethod.exec(info, methodId, models);
        }
        return false;
    }

    private static <T> void fillValue(ModelInfo<T> info, List<? extends T> models) {
        for (ModelField<T> field : info.getInsertFields()) {
            for (T model : models) {
                field.autoFillValue(model);
            }
        }
    }

    private static String methodId(ModelInfo<?> info) {
        return ModelMethodDefinition.methodId(info, METHOD_NAME);
    }

}
