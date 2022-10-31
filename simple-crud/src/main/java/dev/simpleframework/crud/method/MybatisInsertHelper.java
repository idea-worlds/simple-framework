package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.util.MybatisHelper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
final class MybatisInsertHelper {

    static <T> boolean insert(ModelInfo<?> info, T model) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "insert",
                    SqlCommandType.INSERT, Integer.class, INSERT);
            return session.insert(msId, model) == 1;
        });
    }

    static <T> boolean insertBatch(ModelInfo<?> info, List<? extends T> models) {
        return MybatisHelper.exec(info.config(), session -> {
            String msId = MybatisHelper.mappedStatement(info, session, "insertBatch",
                    SqlCommandType.INSERT, Integer.class, INSERT_BATCH);
            Map<String, Object> param = new HashMap<>(3);
            param.put("list", models);
            return session.insert(msId, param) > 0;
        });
    }

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> INSERT = (info, config) ->
            MybatisHelper.buildSqlSource(info, config, () -> {
                List<? extends ModelField<?>> fields = info.getInsertFields();
                String columnScript = fields.stream()
                        .map(field -> MybatisScripts.wrapperIf(field, field.columnName() + ","))
                        .collect(Collectors.joining("\n"));
                columnScript = String.format("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">%s</trim>", columnScript);

                String fieldScript = fields.stream()
                        .map(field -> MybatisScripts.wrapperIf(field, "#{" + field.fieldName() + "},"))
                        .collect(Collectors.joining("\n"));
                fieldScript = String.format("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">%s</trim>", fieldScript);

                return String.format("<script>INSERT INTO `%s` \n%s\n VALUES \n%s\n</script>",
                        info.name(), columnScript, fieldScript);
            });

    private static final BiFunction<ModelInfo<?>, Configuration, SqlSource> INSERT_BATCH = (info, config) ->
            MybatisHelper.buildSqlSource(info, config, () -> {
                List<? extends ModelField<?>> fields = info.getInsertFields();
                String columnScript = fields.stream()
                        .map(ModelField::columnName)
                        .collect(Collectors.joining(","));

                String fieldScript = fields.stream()
                        .map(field -> "#{et." + field.fieldName() + "}")
                        .collect(Collectors.joining(","));
                fieldScript = String.format("<foreach collection=\"list\" item=\"et\" separator=\",\">(%s)</foreach>", fieldScript);

                return String.format("<script>INSERT INTO `%s` \n(%s)\n VALUES \n%s\n</script>",
                        info.name(), columnScript, fieldScript);
            });

}
