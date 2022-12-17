package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.util.MybatisHelper;
import dev.simpleframework.crud.util.MybatisTypeHandler;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MybatisInsertMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.INSERT, Integer.class,
                param -> {
                    List<? extends ModelField<?>> fields = info.getInsertFields();
                    String columnScript = fields.stream()
                            .map(field -> MybatisScripts.wrapperIf(field, field.columnName() + ","))
                            .collect(Collectors.joining("\n"));
                    columnScript = String.format("\n<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n%s\n</trim>\n", columnScript);

                    String fieldScript = fields.stream()
                            .map(field -> {
                                String fieldName = MybatisTypeHandler.resolveFieldName(field, field.fieldName());
                                return MybatisScripts.wrapperIf(field, String.format("#{%s},", fieldName));
                            })
                            .collect(Collectors.joining("\n"));
                    fieldScript = String.format("\n<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n%s\n</trim>\n", fieldScript);

                    return String.format("<script>INSERT INTO %s %s VALUES %s</script>",
                            info.name(), columnScript, fieldScript);
                });
    }

    public static <T> boolean exec(ModelInfo<?> info, String methodId, T model) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    int result = session.insert(methodId, model);
                    return result == 1;
                }
        );
    }

}
