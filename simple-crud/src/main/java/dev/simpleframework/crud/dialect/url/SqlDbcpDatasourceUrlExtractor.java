package dev.simpleframework.crud.dialect.url;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SqlDbcpDatasourceUrlExtractor extends SqlDataSourceDatasourceUrlExtractor<BasicDataSource> {

    @Override
    public String getUrl(BasicDataSource datasource) {
        return datasource.getUrl();
    }

}