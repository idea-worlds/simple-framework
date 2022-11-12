package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.util.MybatisHelper;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.apache.ibatis.mapping.SqlCommandType.DELETE;

@SuppressWarnings("unchecked")
final class MybatisDeleteHelper {

    static <T> boolean deleteById(ModelInfo<?> info, Object id) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "deleteById",
                    DELETE, Integer.class, DEL_BY_ID);
            Map<String, Object> param = new HashMap<>(3);
            param.put("id", id);
            return session.delete(msId, param) == 1;
        });
    }

    static <T> boolean deleteByIds(ModelInfo<?> info, Collection<?> ids) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "deleteByIds",
                    DELETE, Integer.class, DEL_BY_IDS);
            Map<String, Object> param = new HashMap<>(3);
            param.put("ids", ids);
            return session.delete(msId, param) > 0;
        });
    }

    static <T> int deleteByConditions(ModelInfo<?> info, T model) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "deleteByStaticConditions",
                    DELETE, Integer.class, DEL_BY_CONS);
            Map<String, Object> param = new HashMap<>(3);
            param.put("model", model);
            return session.delete(msId, param);
        });
    }

    static <T> int deleteByConditionsDynamic(ModelInfo<?> info, T model, QueryConditions conditions) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "deleteByDynamicConditions",
                    DELETE, Integer.class, DEL_BY_CONS_DYNAMIC);
            Map<String, Object> param = new HashMap<>(3);
            param.put("model", model);
            param.put("config", conditions);
            param.put("data", conditions.getConditionData());
            return session.delete(msId, param);
        });
    }

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> DEL_BY_ID = (info, config) ->
            MybatisHelper.buildSqlSource(info, config, () -> {
                ModelField<?> idField = info.id();
                return String.format("<script>DELETE FROM %s WHERE %s = #{id}</script>",
                        info.name(), idField.columnName());
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> DEL_BY_IDS = (info, config) ->
            MybatisHelper.buildSqlSource(info, config, () -> {
                ModelField<?> idField = info.id();
                String condition = MybatisScripts.foreach("ids", "id");
                return String.format("<script>DELETE FROM %s WHERE %s IN %s</script>",
                        info.name(), idField.columnName(), condition);
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> DEL_BY_CONS = (info, config) ->
            MybatisHelper.buildDynamicSqlSource(config, p -> {
                String condition = MybatisScripts.conditionScript(info.getAllFields());
                return String.format("<script>DELETE FROM %s %s</script>",
                        info.name(), condition);
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> DEL_BY_CONS_DYNAMIC = (info, config) ->
            MybatisHelper.buildDynamicSqlSource(config, p -> {
                Map<String, Object> param = (Map<String, Object>) p;
                QueryConditions conditions = (QueryConditions) param.get("config");
                String condition = MybatisScripts.conditionScript(info.getAllFields(), conditions);
                return String.format("<script>DELETE FROM %s %s</script>",
                        info.name(), condition);
            });

}
