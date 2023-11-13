package dev.simpleframework.crud.helper.provider;

import dev.simpleframework.core.util.SimpleSpringUtils;
import dev.simpleframework.core.util.Strings;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.helper.DatasourceProvider;
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
