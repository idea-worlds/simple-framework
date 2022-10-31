package dev.simpleframework.crud.core;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
@Accessors(fluent = true)
public class ModelConfiguration {

    /**
     * 模型类型
     */
    private final DatasourceType datasourceType;
    /**
     * 数据源名
     * type=Mybatis 时为 org.apache.ibatis.session.SqlSession 的 spring bean name
     */
    private final String datasourceName;
    /**
     * 模型表名转换策略
     */
    private final ModelNameStrategy tableNameStrategy;
    /**
     * 模型字段名转换策略
     */
    private final ModelNameStrategy columnNameStrategy;


    public ModelConfiguration() {
        this(null, null, null, null);
    }

    public ModelConfiguration(DatasourceType dsType) {
        this(dsType, null, null, null);
    }

    public ModelConfiguration(DatasourceType dsType, String dsName) {
        this(dsType, dsName, null, null);
    }

    public ModelConfiguration(DatasourceType dsType, String dsName, ModelNameStrategy nameStrategy) {
        this(dsType, dsName, nameStrategy, nameStrategy);
    }

    public ModelConfiguration(DatasourceType dsType, String dsName, ModelNameStrategy table, ModelNameStrategy column) {
        this.datasourceType = dsType != null ? dsType : DatasourceType.Mybatis;
        this.datasourceName = dsName != null ? dsName : "";
        this.tableNameStrategy = table != null ? table : ModelNameStrategy.UNDERLINE_LOWER_CASE;
        this.columnNameStrategy = column != null ? column : ModelNameStrategy.UNDERLINE_LOWER_CASE;
    }

}
