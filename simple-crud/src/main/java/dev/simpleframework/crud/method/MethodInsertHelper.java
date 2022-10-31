package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.DatasourceType;

import java.util.Collection;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MethodInsertHelper {

    public static <T> boolean insert(T model) {
        if (model == null) {
            return false;
        }
        ModelInfo<T> info = Models.info(model);
        DatasourceType datasourceType = info.config().datasourceType();
        setIdValueIfAbsent(info, model);

        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisInsertHelper.insert(info, model);
        }
        return false;
    }

    public static <T> boolean insertBatch(List<? extends T> models) {
        if (models.isEmpty()) {
            return false;
        }
        ModelInfo<T> info = Models.info(models.get(0));
        DatasourceType datasourceType = info.config().datasourceType();
        setIdValueIfAbsent(info, models);

        if (datasourceType == DatasourceType.Mybatis) {
            return MybatisInsertHelper.insertBatch(info, models);
        }
        return false;
    }

    private static <T> void setIdValueIfAbsent(ModelInfo<T> info, T model) {
        ModelField<T> id = info.id();
        if (id == null) {
            return;
        }
        id.setValue(model, null);
    }

    private static <T> void setIdValueIfAbsent(ModelInfo<T> info, Collection<? extends T> model) {
        ModelField<T> id = info.id();
        if (id == null) {
            return;
        }
        for (T m : model) {
            id.setValue(m, null);
        }
    }

}
