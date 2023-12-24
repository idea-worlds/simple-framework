package dev.simpleframework.crud.dialect.url;

import javax.sql.DataSource;
import java.lang.reflect.ParameterizedType;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class SqlDataSourceDatasourceUrlExtractor<T extends DataSource> implements DatasourceUrlExtractor {
    protected Class<?> datasourceClass;

    public SqlDataSourceDatasourceUrlExtractor() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        this.datasourceClass = (Class<?>) type.getActualTypeArguments()[0];
    }

    public abstract String getUrl(T datasource);

    @Override
    public String extract(Object datasource) {
        return this.datasourceClass.isInstance(datasource) ?
                getUrl((T) datasource) : null;
    }

}
