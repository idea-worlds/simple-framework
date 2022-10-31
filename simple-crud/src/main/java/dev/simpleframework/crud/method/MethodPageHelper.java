package dev.simpleframework.crud.method;

import dev.simpleframework.crud.core.Page;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.exception.SimpleCrudException;
import dev.simpleframework.crud.util.MybatisHelper;
import dev.simpleframework.crud.util.Constants;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MethodPageHelper {

    public static <T, R extends T> Page<R> pageByConditions(T model, int pageNum, int pageSize, QueryConfig... configs) {
        QueryConfig queryConfig = QueryConfig.combineConfigs(configs);
        long total = MethodFindHelper.countByConditions(model, queryConfig.getConditions());
        if (total == 0) {
            return Page.of(pageNum, pageSize, total);
        }
        if (Constants.pageHelperPresent) {
            return MybatisHelper.doSelectPage(pageNum, pageSize, () -> MethodFindHelper.listByConditions(model, queryConfig), total);
        }
        throw new SimpleCrudException("PageByConditions only support PageHelper");
    }

}
