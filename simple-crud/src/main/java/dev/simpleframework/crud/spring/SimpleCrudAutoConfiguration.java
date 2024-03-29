package dev.simpleframework.crud.spring;

import dev.simpleframework.crud.helper.DataFillStrategy;
import dev.simpleframework.crud.helper.DatasourceProvider;
import dev.simpleframework.crud.helper.provider.DefaultSpringMybatisProvider;
import dev.simpleframework.crud.helper.strategy.DefaultDataIdFillStrategy;
import dev.simpleframework.crud.helper.strategy.DefaultDataOperateDateFillStrategy;
import dev.simpleframework.crud.util.ModelCache;
import dev.simpleframework.crud.util.ModelRegistrar;
import dev.simpleframework.util.SimpleSpringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCrudAutoConfiguration implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setDatasourceProvider();
        this.setDataFillStrategy();
        ModelRegistrar.register();
    }

    private void setDatasourceProvider() {
        DatasourceProvider<SqlSession> defaultMybatisProvider = new DefaultSpringMybatisProvider();
        ModelCache.registerProvider(defaultMybatisProvider);
        SimpleSpringUtils.getBeans(DatasourceProvider.class).forEach(ModelCache::registerProvider);
    }

    private void setDataFillStrategy() {
        ModelCache.registerFillStrategy(new DefaultDataIdFillStrategy());
        ModelCache.registerFillStrategy(new DefaultDataOperateDateFillStrategy());
        SimpleSpringUtils.getBeans(DataFillStrategy.class).forEach(ModelCache::registerFillStrategy);
    }

}
