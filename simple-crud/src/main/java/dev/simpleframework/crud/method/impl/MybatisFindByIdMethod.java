package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.dialect.condition.SqlConditionDialect;
import dev.simpleframework.crud.util.MybatisHelper;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("unchecked")
public final class MybatisFindByIdMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.SELECT, info.modelClass(),
                (configuration, param) -> {
                    Map<String, Object> params = (Map<String, Object>) param;
                    QueryFields fields = (QueryFields) params.get("config");
                    String column = MybatisScripts.columnScript(fields.find(info.getSelectFields()));
                    String condition = SqlConditionDialect.DEFAULT.equal(info.id(), "#{id}", true);
                    return String.format("<script>SELECT %s FROM %s WHERE %s</script>",
                            column, info.name(), condition);
                });
    }

    public static <T, R extends T> R exec(ModelInfo<?> info, String methodId, Object id, QueryFields queryFields) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    Map<String, Object> param = new HashMap<>(3);
                    param.put("id", id);
                    param.put("config", queryFields);
                    return session.selectOne(methodId, param);
                }
        );
    }

}
