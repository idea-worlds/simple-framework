package dev.simpleframework.crud.spring;

import dev.simpleframework.crud.DatasourceProvider;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.util.SimpleSpringUtils;
import dev.simpleframework.util.Strings;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
public class SimpleCrudAutoConfiguration implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        setModelDatasourceProvider();
    }

    private void setModelDatasourceProvider() {
        DatasourceProvider<SqlSession> provider = new DatasourceProvider<SqlSession>() {
            @Override
            public SqlSession get(String name) {
                return Strings.hasText(name) ?
                        SimpleSpringUtils.getBean(name, SqlSession.class)
                        :
                        SimpleSpringUtils.getBean(SqlSession.class);
            }

            @Override
            public boolean closeable(String name) {
                return false;
            }
        };
        Models.registerProvider(DatasourceType.Mybatis, provider);
    }

}
