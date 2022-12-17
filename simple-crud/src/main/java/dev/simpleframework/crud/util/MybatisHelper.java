package dev.simpleframework.crud.util;

import com.github.pagehelper.PageHelper;
import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.Page;
import dev.simpleframework.crud.helper.DatasourceProvider;
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
        DatasourceProvider<SqlSession> provider = ModelCache.provider(datasourceType);
        SqlSession session = provider.get(datasourceName);
        try {
            return function.apply(session);
        } finally {
            if (provider.closeable(datasourceName)) {
                session.close();
            }
        }
    }

    public static void addMappedStatement(ModelInfo<?> info,
                                          String methodId,
                                          SqlCommandType commandType,
                                          Class<?> resultType,
                                          Function<Object, String> sqlProvider) {
        DatasourceProvider<SqlSession> provider = ModelCache.provider(DatasourceType.Mybatis);
        SqlSession sqlSession = provider.get(info.datasourceName());
        try {
            Configuration configuration = sqlSession.getConfiguration();
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
            SqlSource sqlSource = buildSqlSource(configuration, sqlProvider);
            MappedStatement ms = new MappedStatement.Builder(configuration, methodId, sqlSource, commandType)
                    .resultMaps(Collections.singletonList(
                            new ResultMap.Builder(configuration, methodId, resultType, resultMappings).build()
                    ))
                    .keyGenerator(keyGenerator)
                    .keyColumn(keyColumn)
                    .keyProperty(keyFieldName)
                    .build();
            configuration.addMappedStatement(ms);
        } finally {
            if (provider.closeable(info.datasourceName())) {
                sqlSession.close();
            }
        }
    }

    private static SqlSource buildSqlSource(Configuration config, Function<Object, String> sqlProvider) {
        return param -> config
                .getDefaultScriptingLanguageInstance()
                .createSqlSource(config, sqlProvider.apply(param), null)
                .getBoundSql(param);
    }

}
