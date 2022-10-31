package dev.simpleframework.crud.util;

import com.github.pagehelper.PageHelper;
import dev.simpleframework.crud.DatasourceProvider;
import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.ModelConfiguration;
import dev.simpleframework.crud.core.Page;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.apache.ibatis.mapping.SqlCommandType.INSERT;

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
            return Page.of(pageNum, pageSize, total,page.getResult());
        }
    }

    public static <R> R exec(ModelConfiguration config, Function<SqlSession, R> function) {
        String datasourceName = config.datasourceName();
        DatasourceType datasourceType = config.datasourceType();

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
        SqlSource sqlSource = sqlSourceProvider.apply(info, configuration);
        MappedStatement ms = new MappedStatement.Builder(configuration, msId, sqlSource, commandType)
                .resultMaps(Collections.singletonList(
                        new ResultMap.Builder(configuration, msId, resultType, new ArrayList<>()).build()
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
