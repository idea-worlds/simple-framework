package com.example.myapp;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;

/**
 * 第二数据源测试配置，用于多数据源集成测试。
 */
@Configuration
public class MultiDsTestConfig {

    @Bean
    public DataSource secondDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl("jdbc:h2:mem:seconddb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        ds.setDriverClassName("org.h2.Driver");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean
    public SqlSessionFactory secondSqlSessionFactory(DataSource secondDataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(secondDataSource);
        return factory.getObject();
    }

    @Bean("second")
    public SqlSessionTemplate secondSqlSession(SqlSessionFactory secondSqlSessionFactory) {
        return new SqlSessionTemplate(secondSqlSessionFactory);
    }

    /**
     * 为第二数据源执行 schema 初始化（Spring Boot 默认只处理主数据源）。
     */
    @Bean
    public DataSourceScriptDatabaseInitializer secondDbInitializer(DataSource secondDataSource) {
        var settings = new DatabaseInitializationSettings();
        settings.setSchemaLocations(List.of("classpath:second-schema.sql"));
        return new DataSourceScriptDatabaseInitializer(secondDataSource, settings);
    }

}
