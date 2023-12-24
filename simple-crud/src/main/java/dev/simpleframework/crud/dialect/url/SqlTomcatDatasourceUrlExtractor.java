package dev.simpleframework.crud.dialect.url;

import org.apache.tomcat.jdbc.pool.DataSource;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SqlTomcatDatasourceUrlExtractor extends SqlDataSourceDatasourceUrlExtractor<DataSource> {

    @Override
    public String getUrl(DataSource datasource) {
        return datasource.getUrl();
    }

}