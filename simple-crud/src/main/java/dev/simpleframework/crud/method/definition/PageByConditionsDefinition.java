package dev.simpleframework.crud.method.definition;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.Page;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.method.ModelMethodDefinition;
import dev.simpleframework.crud.util.Constants;
import dev.simpleframework.crud.util.ModelCache;
import dev.simpleframework.crud.util.MybatisHelper;

public class PageByConditionsDefinition implements ModelMethodDefinition {
    public static final String METHOD_NAME = "pageByConditions";

    @Override
    public void register(ModelInfo<?> info) {
        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            // nothing
        } else {
            throw new ModelRegisterException(info.modelClass(), info.datasourceType());
        }
    }

    public static <T, R extends T> Page<R> exec(T model, int pageNum, int pageSize, QueryConfig... configs) {
        QueryConfig queryConfig = QueryConfig.combineConfigs(configs);
        long total = CountByConditionsDefinition.exec(model, queryConfig.getConditions());
        if (total == 0) {
            return Page.of(pageNum, pageSize, total);
        }
        ModelInfo<T> info = ModelCache.info(model);

        String methodId = methodId(info);
        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType == DatasourceType.Mybatis) {
            if (Constants.pageHelperPresent) {
                return MybatisHelper.doSelectPage(pageNum, pageSize, () -> ListByConditionsDefinition.exec(model, queryConfig), total);
            }
        }
        throw new ModelRegisterException(info.modelClass(), "PageByConditions only support PageHelper");
    }

    private static String methodId(ModelInfo<?> info) {
        return ModelMethodDefinition.methodId(info, METHOD_NAME);
    }

}
