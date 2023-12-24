package dev.simpleframework.crud.dialect.url;

/**
 * 数据源链接字符串抽取器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@FunctionalInterface
public interface DatasourceUrlExtractor {

    /**
     * 数据源链接字符串
     */
    String extract(Object datasource);

}
