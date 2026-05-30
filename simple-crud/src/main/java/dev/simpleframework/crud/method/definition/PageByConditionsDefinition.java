package dev.simpleframework.crud.method.definition;

import com.github.pagehelper.PageInterceptor;
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
        if (datasourceType != DatasourceType.Mybatis) {
            throw new ModelRegisterException(info.modelClass(), info.datasourceType());
        }
    }

    public static <T, R extends T> Page<R> exec(T model, int pageNum, int pageSize, boolean needCount, QueryConfig queryConfig) {
        ModelInfo<T> info = ModelCache.info(model);

        DatasourceType datasourceType = info.datasourceType();
        if (datasourceType != DatasourceType.Mybatis) {
            throw new ModelRegisterException(info.modelClass(), "PageByConditions only support MyBatis");
        }
        if (!Constants.pageHelperPresent) {
            throw new ModelRegisterException(info.modelClass(), "PageByConditions only support PageHelper");
        }
        if (!MybatisHelper.hasInterceptor(datasourceType, info.datasourceName(), PageInterceptor.class)) {
            throw new ModelRegisterException(info.modelClass(),
                    "PageByConditions requires PageHelper PageInterceptor to be registered in MyBatis Configuration");
        }

        long total = needCount ? CountByConditionsDefinition.exec(model, queryConfig.getConditions()) : 0;
        if (needCount && total == 0) {
            return Page.of(pageNum, pageSize, total);
        }
        return MybatisHelper.doSelectPage(pageNum, pageSize, () -> ListByConditionsDefinition.exec(model, queryConfig), total);
    }

    private static String methodId(ModelInfo<?> info) {
        return ModelMethodDefinition.methodId(info, METHOD_NAME);
    }

}
