package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.util.MybatisHelper;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("unchecked")
public final class MybatisListByConditionsMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.SELECT, info.modelClass(),
                (configuration, param) -> {
                    Map<String, Object> params = (Map<String, Object>) param;
                    QueryConfig queryConfig = (QueryConfig) params.get("config");
                    List<? extends ModelField<?>> allFields = info.getAllFields();
                    String column = MybatisScripts.columnScript(queryConfig.getFields().find(info.getSelectFields()));
                    String condition = MybatisScripts.conditionScript(configuration, allFields, queryConfig.getConditions());
                    String sort = MybatisScripts.sortScript(allFields, queryConfig.getSorters());
                    return String.format("<script>SELECT %s FROM %s %s %s</script>",
                            column, info.name(), condition, sort);
                });
    }

    public static <T, R extends T> List<R> exec(ModelInfo<?> info, String methodId, T model, QueryConfig queryConfig) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    QueryConditions conditions = queryConfig.getConditions();
                    Map<String, Object> conditionData = conditions == null ?
                            Collections.emptyMap() : conditions.getConditionData();
                    Map<String, Object> param = new HashMap<>(6);
                    param.put("model", model);
                    param.put("config", queryConfig);
                    param.put("data", conditionData);
                    return session.selectList(methodId, param);
                }
        );
    }

}
