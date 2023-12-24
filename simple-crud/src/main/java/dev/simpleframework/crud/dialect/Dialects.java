package dev.simpleframework.crud.dialect;

import dev.simpleframework.crud.dialect.condition.ConditionDialect;
import dev.simpleframework.crud.dialect.condition.SqlConditionDialect;
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

    public static ConditionDialect condition(DataSource datasource) {
        if (JDBC_CONDITIONS.isEmpty()) {
            return SqlConditionDialect.DEFAULT;
        }
        String url = extractUrl(datasource);
        if (Strings.isBlank(url)) {
            return SqlConditionDialect.DEFAULT;
        }
        url = url.toLowerCase();
        for (Map.Entry<String, ConditionDialect> entry : JDBC_CONDITIONS.entrySet()) {
            if (url.contains(":" + entry.getKey() + ":")) {
                return entry.getValue();
            }
        }
        return SqlConditionDialect.DEFAULT;
    }

}
