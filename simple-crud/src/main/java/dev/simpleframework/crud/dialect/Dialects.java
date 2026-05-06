package dev.simpleframework.crud.dialect;

import dev.simpleframework.crud.dialect.condition.ConditionDialect;
import dev.simpleframework.crud.dialect.condition.H2ConditionDialect;
import dev.simpleframework.crud.dialect.condition.MySqlConditionDialect;
import dev.simpleframework.crud.dialect.condition.OracleConditionDialect;
import dev.simpleframework.crud.dialect.condition.PgConditionDialect;
import dev.simpleframework.crud.dialect.url.*;
import dev.simpleframework.util.Strings;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class Dialects {
    private static final List<SqlDataSourceDatasourceUrlExtractor<?>> DS_URLS = new ArrayList<>();
    private static final Map<String, ConditionDialect> JDBC_CONDITIONS = new LinkedHashMap<>();
    private static volatile boolean quoteColumnNames = false;

    static {
        try {
            DS_URLS.add(new SqlHikariDatasourceUrlExtractor());
        } catch (Exception ignore) {
        }
        try {
            DS_URLS.add(new SqlDruidDatasourceUrlExtractor());
        } catch (Exception ignore) {
        }
        try {
            DS_URLS.add(new SqlDbcpDatasourceUrlExtractor());
        } catch (Exception ignore) {
        }
        try {
            DS_URLS.add(new SqlTomcatDatasourceUrlExtractor());
        } catch (Exception ignore) {
        }

        // 预注册数据库方言（通过 JDBC URL 子协议自动匹配）
        registerConditionDialect("postgresql", PgConditionDialect.DEFAULT);
        registerConditionDialect("mysql", MySqlConditionDialect.DEFAULT);
        registerConditionDialect("h2", H2ConditionDialect.DEFAULT);
        registerConditionDialect("oracle", OracleConditionDialect.DEFAULT);
    }

    /**
     * 注册数据库条件方言
     */
    public static void registerConditionDialect(String db, ConditionDialect dialect) {
        JDBC_CONDITIONS.put(db, dialect);
    }

    public static String extractUrl(DataSource datasource) {
        String url;
        for (SqlDataSourceDatasourceUrlExtractor<?> builder : DS_URLS) {
            url = builder.extract(datasource);
            if (url == null) {
                continue;
            }
            return url;
        }
        return null;
    }

    /**
     * 根据 DataSource 的 JDBC URL 匹配对应的 SQL 方言。
     * <p>
     * 匹配规则：从 URL 中提取数据库标识（如 {@code :mysql:}、{@code :postgresql:}），
     * 按 {@link #registerConditionDialect} 注册顺序依次匹配，首个命中的方言生效。
     * 无匹配时回退到 {@link PgConditionDialect#DEFAULT}。
     */
    public static ConditionDialect condition(DataSource datasource) {
        if (JDBC_CONDITIONS.isEmpty()) {
            return PgConditionDialect.DEFAULT;
        }
        String url = extractUrl(datasource);
        if (Strings.isBlank(url)) {
            return PgConditionDialect.DEFAULT;
        }
        url = url.toLowerCase();
        for (Map.Entry<String, ConditionDialect> entry : JDBC_CONDITIONS.entrySet()) {
            if (url.contains(":" + entry.getKey() + ":")) {
                return entry.getValue();
            }
        }
        return PgConditionDialect.DEFAULT;
    }

    /**
     * 是否使用带引号的列名，解决字段名与 SQL 关键字冲突问题。
     * 默认 false。开启后 {@code @Column(name)} 必须与 DB 实际列名大小写一致。
     */
    public static void setQuoteColumnNames(boolean quote) {
        Dialects.quoteColumnNames = quote;
    }

    public static boolean isQuoteColumnNames() {
        return quoteColumnNames;
    }

}
