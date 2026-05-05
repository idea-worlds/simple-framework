package dev.simpleframework.crud.spring;

import dev.simpleframework.crud.core.FieldCustomizer;
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
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCrudAutoConfiguration implements InitializingBean {
    private volatile boolean modelRegistered;

    @Override
    public void afterPropertiesSet() {
        this.setDatasourceProvider();
        this.setDataFillStrategy();
    }

    /**
     * 在 Spring 容器 refresh 完成后注册模型并应用字段覆盖。
     * 此时 SqlSession 等 MyBatis bean 已就绪，{@link #afterPropertiesSet} 中如果立即注册，
     * 会因为 MyBatis 自动配置未完成导致 {@code getBean(SqlSession.class)} 失败。
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        if (modelRegistered) {
            return;
        }
        modelRegistered = true;
        ModelRegistrar.register();
        this.applyFieldCustomizers();
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

    private void applyFieldCustomizers() {
        SimpleSpringUtils.getBeans(FieldCustomizer.class).forEach(FieldCustomizer::apply);
    }

}
