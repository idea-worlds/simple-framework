package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.util.MybatisHelper;
import dev.simpleframework.crud.util.MybatisTypeHandler;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class MybatisUpdateByIdMethod {

    public static void register(ModelInfo<?> info, String methodId) {
        MybatisHelper.addMappedStatement(info, methodId, SqlCommandType.UPDATE, Integer.class,
                param -> {
                    ModelField<?> idField = info.id();
                    String script = info.getUpdateFields().stream()
                            .map(field -> {
                                String fieldName = MybatisTypeHandler.resolveFieldName(field, field.fieldName());
                                String tmp = String.format("%s = #{%s},", field.columnName(), fieldName);
                                return MybatisScripts.wrapperIf(field, tmp);
                            })
                            .collect(Collectors.joining("\n"));
                    return String.format("<script>UPDATE %s \n<set>\n%s\n</set>\n WHERE %s = #{%s}</script>",
                            info.name(), script, idField.columnName(), idField.fieldName());
                });
    }

    public static <T> boolean exec(ModelInfo<?> info, String methodId, T model) {
        return MybatisHelper.exec(info.datasourceType(), info.datasourceName(),
                session -> {
                    return session.update(methodId, model) == 1;
                }
        );
    }

}
