package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.util.MybatisHelper;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MybatisDeleteByIdsMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.DELETE, Integer.class,
                param -> {
                    ModelField<?> idField = info.id();
                    String condition = MybatisScripts.foreach("ids", "_id");
                    return String.format("<script>DELETE FROM %s WHERE %s IN %s</script>",
                            info.name(), idField.columnName(), condition);
                });
    }

    public static boolean exec(ModelInfo<?> info, String methodId, Collection<?> ids) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    Map<String, Object> param = new HashMap<>(3);
                    param.put("ids", ids);
                    return session.delete(methodId, param) > 0;
                }
        );
    }

}
