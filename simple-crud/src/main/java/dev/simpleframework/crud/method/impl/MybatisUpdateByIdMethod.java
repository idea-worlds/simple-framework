package dev.simpleframework.crud.method.impl;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.dialect.condition.SqlConditionDialect;
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
                (configuration, param) -> {
                    String script = info.getUpdateFields().stream()
                            .map(field -> {
                                String fieldName = MybatisTypeHandler.resolveFieldName(field, field.fieldName());
                                String tmp = String.format("%s = #{%s},", field.columnName(), fieldName);
                                return MybatisScripts.wrapperIf(field, tmp);
                            })
                            .collect(Collectors.joining("\n"));
                    ModelField<?> id = info.id();
                    String idParam = "#{" + id.fieldName() + "}";
                    String condition = SqlConditionDialect.DEFAULT.equal(info.id(), idParam, true);
                    return String.format("<script>UPDATE %s \n<set>\n%s\n</set>\n WHERE %s</script>",
                            info.name(), script, condition);
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
