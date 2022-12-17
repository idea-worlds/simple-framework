package dev.simpleframework.crud.helper.provider;

import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.helper.DatasourceProvider;
import dev.simpleframework.util.SimpleSpringUtils;
import dev.simpleframework.util.Strings;
import org.apache.ibatis.session.SqlSession;

public class DefaultSpringMybatisProvider implements DatasourceProvider<SqlSession> {

    @Override
    public SqlSession get(String name) {
        return Strings.hasText(name) ?
                SimpleSpringUtils.getBean(name, SqlSession.class)
                :
                SimpleSpringUtils.getBean(SqlSession.class);
    }

    @Override
    public DatasourceType support() {
        return DatasourceType.Mybatis;
    }

    @Override
    public boolean closeable(String name) {
        return false;
    }

}
