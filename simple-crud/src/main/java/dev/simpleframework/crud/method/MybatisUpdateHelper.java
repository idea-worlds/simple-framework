package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.util.MybatisHelper;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.apache.ibatis.mapping.SqlCommandType.UPDATE;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("unchecked")
final class MybatisUpdateHelper {

    static <T> boolean updateById(ModelInfo<?> info, T model) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "updateById",
                    UPDATE, Integer.class, UPDATE_BY_ID);
            return session.update(msId, model) == 1;
        });
    }

    static <T> int updateByConditions(ModelInfo<?> info, T model, QueryConditions conditions) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "updateByConditions",
                    UPDATE, Integer.class, UPDATE_BY_CONS);
            Map<String, Object> param = new HashMap<>(6);
            param.put("model", model);
            param.put("config", conditions);
            param.put("data", conditions.getConditionData());
            return session.update(msId, param);
        });
    }

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> UPDATE_BY_ID = (info, config) ->
            MybatisHelper.buildSqlSource(info, config, () -> {
                ModelField<?> idField = info.id();
                String script = info.getUpdateFields().stream()
                        .map(field -> MybatisScripts.wrapperIf(field, MybatisScripts.columnEqual(field) + ","))
                        .collect(Collectors.joining("\n"));
                return String.format("<script>UPDATE `%s` \n<set>%s</set>\n WHERE %s</script>",
                        info.name(), script, MybatisScripts.columnEqual(idField));
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> UPDATE_BY_CONS = (info, config) ->
            MybatisHelper.buildDynamicSqlSource(config, p -> {
                Map<String, Object> param = (Map<String, Object>) p;
                QueryConditions conditions = (QueryConditions) param.get("config");
                String column = info.getUpdateFields().stream()
                        .map(field -> {
                            String fieldName = "model." + field.fieldName();
                            String tmp = String.format("`%s` = #{%s},", field.columnName(), fieldName);
                            return MybatisScripts.wrapperIf(fieldName, tmp);
                        })
                        .collect(Collectors.joining("\n"));
                String condition = MybatisScripts.conditionScript(info.getAllFields(), conditions);
                return String.format("<script>UPDATE `%s` \n<set>%s</set>\n %s</script>",
                        info.name(), column, condition);
            });

}
