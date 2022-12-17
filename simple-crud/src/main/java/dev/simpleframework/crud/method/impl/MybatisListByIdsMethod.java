package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.util.MybatisHelper;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("unchecked")
public final class MybatisListByIdsMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.SELECT, info.modelClass(),
                param -> {
                    ModelField<?> idField = info.id();
                    Map<String, Object> params = (Map<String, Object>) param;
                    QueryFields fields = (QueryFields) params.get("config");
                    String column = MybatisScripts.columnScript(fields.find(info.getSelectFields()));
                    String condition = MybatisScripts.foreach("ids", "_id");
                    return String.format("<script>SELECT %s FROM %s WHERE \n %s IN \n %s </script>",
                            column, info.name(), idField.columnName(), condition);
                });
    }

    public static <T, R extends T> List<R> exec(ModelInfo<?> info, String methodId, Collection<?> ids, QueryFields queryFields) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    Map<String, Object> param = new HashMap<>(3);
                    param.put("ids", ids);
                    param.put("config", queryFields);
                    return session.selectList(methodId, param);
                }
        );
    }

}
