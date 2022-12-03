package dev.simpleframework.crud.util;

import com.github.pagehelper.PageHelper;
import dev.simpleframework.crud.DatasourceProvider;
import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.Page;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.TypeHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.apache.ibatis.mapping.SqlCommandType.INSERT;
import static org.apache.ibatis.mapping.SqlCommandType.SELECT;

public final class MybatisHelper {

    public static <T> Page<T> doSelectPage(int pageNum, int pageSize, Supplier<List<T>> doSelectList) {
        return doSelectPage(pageNum, pageSize, doSelectList, null);
    }

    public static <T> Page<T> doSelectPage(int pageNum, int pageSize, Supplier<List<T>> doSelectList, long total) {
        return doSelectPage(pageNum, pageSize, doSelectList, () -> total);
    }

    public static <T> Page<T> doSelectPage(int pageNum, int pageSize, Supplier<List<T>> doSelectList, Supplier<Long> doSelectCount) {
        boolean autoCount = doSelectCount == null;
        try (com.github.pagehelper.Page<T> page = PageHelper.startPage(pageNum, pageSize, autoCount)) {
            page.doSelectPage(doSelectList::get);

            long total = autoCount ? page.getTotal() : doSelectCount.get();
            return Page.of(pageNum, pageSize, total, page.getResult());
        }
    }

    public static <R> R exec(DatasourceType datasourceType, String datasourceName, Function<SqlSession, R> function) {
        DatasourceProvider<SqlSession> provider = Models.provider(datasourceType);
        SqlSession session = provider.get(datasourceName);
        try {
            return function.apply(session);
        } finally {
            if (provider.closeable(datasourceName)) {
                session.close();
            }
        }
    }

    public static String mappedStatement(ModelInfo<?> info,
                                         SqlSession sqlSession,
                                         String methodName,
                                         SqlCommandType commandType,
                                         Class<?> resultType,
                                         BiFunction<ModelInfo<?>, Configuration, SqlSource> sqlSourceProvider) {
        String msId = String.format("%s.%s.%s", info.methodNamespace(), commandType, methodName);
        Configuration configuration = sqlSession.getConfiguration();
        if (configuration.hasStatement(msId, false)) {
            return msId;
        }
        String keyColumn = null, keyFieldName = null;
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        ModelField<?> modelId = info.id();
        if (commandType == INSERT && modelId != null && !modelId.insertable()) {
            keyColumn = modelId.columnName();
            keyFieldName = modelId.fieldName();
            keyGenerator = Jdbc3KeyGenerator.INSTANCE;
        }
        List<ResultMapping> resultMappings = new ArrayList<>();
        if (commandType == SELECT && info.modelClass() == resultType) {
            resultMappings = info.getSelectFields()
                    .stream()
                    .map(f -> {
                        TypeHandler<?> handler = MybatisTypeHandler.typeHandler(f);
                        return handler == null ? null :
                                new ResultMapping.Builder(configuration, f.fieldName(), f.columnName(), handler).build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        SqlSource sqlSource = sqlSourceProvider.apply(info, configuration);
        MappedStatement ms = new MappedStatement.Builder(configuration, msId, sqlSource, commandType)
                .resultMaps(Collections.singletonList(
                        new ResultMap.Builder(configuration, msId, resultType, resultMappings).build()
                ))
                .keyGenerator(keyGenerator)
                .keyColumn(keyColumn)
                .keyProperty(keyFieldName)
                .build();
        configuration.addMappedStatement(ms);
        return msId;
    }

    public static SqlSource buildSqlSource(ModelInfo<?> info, Configuration config, Supplier<String> script) {
        if (info.dynamic()) {
            return buildDynamicSqlSource(config, p -> script.get());
        } else {
            return config
                    .getDefaultScriptingLanguageInstance()
                    .createSqlSource(config, script.get(), null);
        }
    }

    public static SqlSource buildDynamicSqlSource(Configuration config, Function<Object, String> script) {
        return param -> config
                .getDefaultScriptingLanguageInstance()
                .createSqlSource(config, script.apply(param), null)
                .getBoundSql(param);
    }

}
