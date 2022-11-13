package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.util.MybatisHelper;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.apache.ibatis.mapping.SqlCommandType.SELECT;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("unchecked")
final class MybatisFindHelper {

    static <T, R extends T> R findById(ModelInfo<?> info, Object id) {
        return MybatisHelper.exec(info.config(), sesssion -> {
            String msId = MybatisHelper.mappedStatement(info, sesssion, "findById",
                    SELECT, info.modelClass(), FIND_BY_ID);
            Map<String, Object> param = new HashMap<>(3);
            param.put("id", id);
            return sesssion.selectOne(msId, param);
        });
    }

    static <T, R extends T> R findByIdDynamic(ModelInfo<?> info, Object id, QueryFields queryFields) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "findByIdWithFields",
                    SELECT, info.modelClass(), FIND_BY_ID_WITH_FIELDS);
            Map<String, Object> param = new HashMap<>(3);
            param.put("id", id);
            param.put("config", queryFields);
            return session.selectOne(msId, param);
        });
    }

    static <T> long countByConditions(ModelInfo<?> info, T model) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "countByStaticConditions",
                    SELECT, Long.class, COUNT_BY_CONS);
            Map<String, Object> param = new HashMap<>(6);
            param.put("model", model);
            Long result = session.selectOne(msId, param);
            return result == null ? 0 : result;
        });
    }

    static <T> long countByConditionsDynamic(ModelInfo<?> info, T model, QueryConditions conditions) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "countByDynamicConditions",
                    SELECT, Long.class, COUNT_BY_CONS_DYNAMIC);
            Map<String, Object> param = new HashMap<>(6);
            param.put("model", model);
            param.put("config", conditions);
            param.put("data", conditions.getConditionData());
            Long result = session.selectOne(msId, param);
            return result == null ? 0 : result;
        });
    }

    static <T, R extends T> List<R> listByIds(ModelInfo<?> info, Collection<?> ids) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "listByIds",
                    SELECT, info.modelClass(), LIST_BY_IDS);
            Map<String, Object> param = new HashMap<>(3);
            param.put("ids", ids);
            return session.selectList(msId, param);
        });
    }

    static <T, R extends T> List<R> listByIdsDynamic(ModelInfo<?> info, Collection<?> ids, QueryFields queryFields) {
        return MybatisHelper.exec(info.config(), sqlSession -> {
            String msId = MybatisHelper.mappedStatement(info, sqlSession, "listByIdsWithFields",
                    SELECT, info.modelClass(), LIST_BY_IDS_WITH_FIELDS);
            Map<String, Object> param = new HashMap<>(3);
            param.put("ids", ids);
            param.put("config", queryFields);
            return sqlSession.selectList(msId, param);
        });
    }

    static <T, R extends T> List<R> listByConditions(ModelInfo<?> info, T model) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "listByStaticConditions",
                    SELECT, info.modelClass(), LIST_BY_CONS);
            Map<String, Object> param = new HashMap<>(6);
            param.put("model", model);
            return session.selectList(msId, param);
        });
    }

    static <T, R extends T> List<R> listByConditionsDynamic(ModelInfo<?> info, T model, QueryConfig queryConfig) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "listByDynamicConditions",
                    SELECT, info.modelClass(), LIST_BY_CONS_DYNAMIC);
            Map<String, Object> param = new HashMap<>(6);
            param.put("model", model);
            param.put("config", queryConfig);
            param.put("data", queryConfig.getConditions().getConditionData());
            return session.selectList(msId, param);
        });
    }

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> FIND_BY_ID = (info, config) ->
            MybatisHelper.buildSqlSource(info, config, () -> {
                ModelField<?> idField = info.id();
                String column = MybatisScripts.columnScript(info.getSelectFields());
                return String.format("<script>SELECT %s FROM %s WHERE %s = #{id}</script>",
                        column, info.name(), idField.columnName());
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> FIND_BY_ID_WITH_FIELDS = (info, config) ->
            MybatisHelper.buildDynamicSqlSource(config, p -> {
                ModelField<?> idField = info.id();
                Map<String, Object> param = (Map<String, Object>) p;
                QueryFields fields = (QueryFields) param.get("config");
                String column = MybatisScripts.columnScript(fields.find(info.getSelectFields()));
                return String.format("<script>SELECT %s FROM %s WHERE %s = #{id}</script>",
                        column, info.name(), idField.columnName());
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> COUNT_BY_CONS = (info, config) ->
            MybatisHelper.buildSqlSource(info, config, () -> {
                String condition = MybatisScripts.conditionScript(info.getAllFields());
                return String.format("<script>SELECT COUNT(*) FROM %s %s</script>",
                        info.name(), condition);
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> COUNT_BY_CONS_DYNAMIC = (info, config) ->
            MybatisHelper.buildDynamicSqlSource(config, p -> {
                Map<String, Object> param = (Map<String, Object>) p;
                QueryConditions conditions = (QueryConditions) param.get("config");
                String condition = MybatisScripts.conditionScript(info.getAllFields(), conditions);
                return String.format("<script>SELECT COUNT(*) FROM %s %s</script>",
                        info.name(), condition);
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> LIST_BY_IDS = (info, config) ->
            MybatisHelper.buildSqlSource(info, config, () -> {
                ModelField<?> idField = info.id();
                String column = MybatisScripts.columnScript(info.getSelectFields());
                String condition = MybatisScripts.foreach("ids", "_id");
                return String.format("<script>SELECT %s FROM %s WHERE \n %s IN \n %s </script>",
                        column, info.name(), idField.columnName(), condition);
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> LIST_BY_IDS_WITH_FIELDS = (info, config) ->
            MybatisHelper.buildDynamicSqlSource(config, p -> {
                ModelField<?> idField = info.id();
                Map<String, Object> param = (Map<String, Object>) p;
                QueryFields fields = (QueryFields) param.get("config");
                String column = MybatisScripts.columnScript(fields.find(info.getSelectFields()));
                String condition = MybatisScripts.foreach("ids", "_id");
                return String.format("<script>SELECT %s FROM %s WHERE \n %s IN \n %s </script>",
                        column, info.name(), idField.columnName(), condition);
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> LIST_BY_CONS = (info, config) ->
            MybatisHelper.buildSqlSource(info, config, () -> {
                String column = MybatisScripts.columnScript(info.getSelectFields());
                String condition = MybatisScripts.conditionScript(info.getAllFields());
                return String.format("<script>SELECT %s FROM %s %s </script>",
                        column, info.name(), condition);
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> LIST_BY_CONS_DYNAMIC = (info, config) ->
            MybatisHelper.buildDynamicSqlSource(config, p -> {
                Map<String, Object> param = (Map<String, Object>) p;
                QueryConfig queryConfig = (QueryConfig) param.get("config");
                List<? extends ModelField<?>> allFields = info.getAllFields();
                String column = MybatisScripts.columnScript(queryConfig.getFields().find(info.getSelectFields()));
                String condition = MybatisScripts.conditionScript(allFields, queryConfig.getConditions());
                String sort = MybatisScripts.sortScript(allFields, queryConfig.getSorters());
                return String.format("<script>SELECT %s FROM %s %s %s</script>",
                        column, info.name(), condition, sort);
            });

}
