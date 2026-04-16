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

    /**
     * 向 MyBatis Configuration 动态注册一条 MappedStatement（模型注册阶段调用，非热路径）。
     * <p>
     * 关键设计点：
     * <ul>
     *   <li><b>动态 SQL</b>：{@code sqlProvider} 是一个 {@code BiFunction}，在每次 SQL 执行时
     *       由 MyBatis {@link SqlSource} 回调，生成含 {@code <if>/<where>/<foreach>} 等
     *       动态标签的 XML 脚本，再由 {@code DefaultScriptingLanguageDriver} 解析为 {@link org.apache.ibatis.mapping.BoundSql}。</li>
     *   <li><b>自增主键</b>：当 INSERT 操作且主键字段 {@code insertable=false} 时，
     *       使用 {@link Jdbc3KeyGenerator}，执行后将数据库自增值回填到实体对象。</li>
     *   <li><b>TypeHandler</b>：SELECT 操作对需要自定义类型转换的字段（如数组、JSON）
     *       注入对应的 {@link TypeHandler}，否则使用 MyBatis 默认映射。</li>
     * </ul>
     * 注意：此方法操作 MyBatis {@code Configuration}（非线程安全），调用方须保证串行执行。
     *
     * @param info        模型元信息
     * @param methodId    MappedStatement 的唯一 ID，格式为 {@code namespace.methodName}
     * @param commandType SQL 类型（INSERT/UPDATE/DELETE/SELECT）
     * @param resultType  返回值类型；SELECT 时若与 modelClass 相同则自动构建 ResultMapping
     * @param sqlProvider 动态 SQL 生成函数，入参为 (Configuration, 运行时参数 Map)，返回 XML 脚本
     */
    public static void addMappedStatement(ModelInfo<?> info,
                                          String methodId,
                                          SqlCommandType commandType,
                                          Class<?> resultType,
                                          BiFunction<Configuration, Object, String> sqlProvider) {
        DatasourceProvider<SqlSession> provider = ModelCache.provider(DatasourceType.Mybatis);
        SqlSession sqlSession = provider.get(info.datasourceName());
        try {
            Configuration configuration = sqlSession.getConfiguration();
            String keyColumn = null, keyFieldName = null;
            KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
            ModelField<?> modelId = info.id();
            // insertable=false 表示主键由数据库生成（AUTO_INCREMENT），需开启 Jdbc3KeyGenerator 回填
            if (commandType == INSERT && modelId != null && !modelId.insertable()) {
                keyColumn = modelId.columnName();
                keyFieldName = modelId.fieldName();
                keyGenerator = Jdbc3KeyGenerator.INSTANCE;
            }
            List<ResultMapping> resultMappings = new ArrayList<>();
            if (commandType == SELECT && info.modelClass() == resultType) {
                // 只为有自定义 TypeHandler 的字段注册 ResultMapping，其余字段由 MyBatis 自动按列名映射
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
            // SqlSource 延迟求值：每次执行 SQL 时才展开动态脚本，避免静态绑定参数
            SqlSource sqlSource = param -> configuration
                    .getDefaultScriptingLanguageInstance()
                    .createSqlSource(configuration, sqlProvider.apply(configuration, param), null)
                    .getBoundSql(param);
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

}
