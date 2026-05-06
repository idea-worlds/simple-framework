package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.dialect.Dialects;
import dev.simpleframework.crud.dialect.condition.ConditionDialect;
import dev.simpleframework.crud.util.MybatisHelper;
import dev.simpleframework.crud.util.MybatisTypeHandler;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MybatisInsertBatchMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.INSERT, Integer.class,
                (configuration, param) -> {
                    ConditionDialect dialect = Dialects.condition(configuration.getEnvironment().getDataSource());
                    List<? extends ModelField<?>> fields = info.getInsertFields();
                    String columnScript = fields.stream()
                            .map(f -> dialect.column(f))
                            .collect(Collectors.joining(","));

                    String fieldScript = fields.stream()
                            .map(field -> {
                                String fieldName = MybatisTypeHandler.resolveFieldName(field, field.fieldName());
                                return String.format("#{et.%s}", fieldName);
                            })
                            .collect(Collectors.joining(","));
                    fieldScript = String.format(
                            "\n<foreach collection=\"list\" item=\"et\" separator=\",\">(%s)</foreach>\n",
                            fieldScript);

                    return String.format("<script>INSERT INTO %s \n(%s)\n VALUES %s</script>",
                            info.name(), columnScript, fieldScript);
                });
    }

    public static <T> boolean exec(ModelInfo<?> info, String insertMethodId, List<T> models) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    for (T model : models) {
                        if (session.insert(insertMethodId, model) != 1) {
                            return false;
                        }
                    }
                    return true;
                }
        );
    }

}
