package dev.simpleframework.crud.dialect.url;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SqlDruidDatasourceUrlExtractor extends SqlDataSourceDatasourceUrlExtractor<DruidDataSource> {

    @Override
    public String getUrl(DruidDataSource datasource) {
        return datasource.getUrl();
    }

}
