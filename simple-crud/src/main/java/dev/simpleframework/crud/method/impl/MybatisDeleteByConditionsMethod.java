package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.util.MybatisHelper;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("unchecked")
public final class MybatisDeleteByConditionsMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.DELETE, Integer.class,
                (configuration, param) -> {
                    Map<String, Object> params = (Map<String, Object>) param;
                    QueryConditions conditions = (QueryConditions) params.get("config");
                    String condition = MybatisScripts.conditionScript(configuration, info.getAllFields(), conditions);
                    return String.format("<script>DELETE FROM %s %s</script>",
                            info.name(), condition);
                });
    }

    public static <T> int exec(ModelInfo<?> info, String methodId, QueryConditions conditions) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    Map<String, Object> param = new HashMap<>(4);
                    param.put("config", conditions);
                    param.put(QueryConditions.KEY_NAME, conditions.getConditionData());
                    return session.delete(methodId, param);
                }
        );
    }

}
