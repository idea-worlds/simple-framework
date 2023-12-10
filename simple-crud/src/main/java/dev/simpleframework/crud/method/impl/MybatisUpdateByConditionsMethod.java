package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.util.MybatisHelper;
import dev.simpleframework.crud.util.MybatisTypeHandler;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("unchecked")
public final class MybatisUpdateByConditionsMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.UPDATE, Integer.class,
                param -> {
                    Map<String, Object> params = (Map<String, Object>) param;
                    QueryConditions conditions = (QueryConditions) params.get("config");
                    String column = info.getUpdateFields().stream()
                            .map(field -> {
                                String fieldName = "model." + field.fieldName();
                                String tmp = String.format("%s = #{%s},", field.columnName(), MybatisTypeHandler.resolveFieldName(field, fieldName));
                                return MybatisScripts.wrapperIf("model", field, tmp);
                            })
                            .collect(Collectors.joining("\n"));
                    String condition = MybatisScripts.conditionScript(info.getAllFields(), conditions);
                    return String.format("<script>UPDATE %s \n<set>\n%s\n</set>\n %s</script>",
                            info.name(), column, condition);
                });
    }

    public static <T> int exec(ModelInfo<?> info, String methodId, T model, QueryConditions conditions) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    Map<String, Object> param = new HashMap<>(6);
                    param.put("model", model);
                    param.put("config", conditions);
                    param.put("data", conditions.getConditionData());
                    return session.update(methodId, param);
                }
        );
    }

}
