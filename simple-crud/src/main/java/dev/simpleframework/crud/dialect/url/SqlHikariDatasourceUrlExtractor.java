package dev.simpleframework.crud.dialect.url;

import com.zaxxer.hikari.HikariDataSource;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SqlHikariDatasourceUrlExtractor extends SqlDataSourceDatasourceUrlExtractor<HikariDataSource> {

    @Override
    public String getUrl(HikariDataSource datasource) {
        return datasource.getJdbcUrl();
    }

}
