package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.util.MybatisHelper;
import dev.simpleframework.crud.util.MybatisTypeHandler;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MybatisInsertBatchMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.INSERT, Integer.class,
                (configuration, param) -> {
                    List<? extends ModelField<?>> fields = info.getInsertFields();
                    String columnScript = fields.stream()
                            .map(ModelField::columnName)
                            .collect(Collectors.joining(","));

                    String fieldScript = fields.stream()
                            .map(field -> {
                                String fieldName = MybatisTypeHandler.resolveFieldName(field, field.fieldName());
                                return MybatisScripts.wrapperIf(field, String.format("#{et.%s}", fieldName));
                            })
                            .collect(Collectors.joining(","));
                    fieldScript = String.format("\n<foreach collection=\"list\" item=\"et\" separator=\",\">(%s)</foreach>\n", fieldScript);

                    return String.format("<script>INSERT INTO %s \n(%s)\n VALUES %s</script>",
                            info.name(), columnScript, fieldScript);
                });
    }

    public static <T> boolean exec(ModelInfo<?> info, String methodId, List<T> models) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    Map<String, Object> param = new HashMap<>(3);
                    param.put("list", models);
                    return session.insert(methodId, param) > 0;
                }
        );
    }

}
