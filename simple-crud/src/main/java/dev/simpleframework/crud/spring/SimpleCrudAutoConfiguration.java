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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Comparator;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCrudAutoConfiguration implements InitializingBean {
    private volatile boolean modelRegistered;

    @Override
    public void afterPropertiesSet() {
        ModelCache.registerProvider(new DefaultSpringMybatisProvider());
        ModelCache.registerFillStrategy(new DefaultDataIdFillStrategy());
        ModelCache.registerFillStrategy(new DefaultDataOperateDateFillStrategy());
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        if (modelRegistered) {
            return;
        }
        modelRegistered = true;
        SimpleSpringUtils.getBeans(DatasourceProvider.class).forEach(ModelCache::registerProvider);
        SimpleSpringUtils.getBeans(DataFillStrategy.class).forEach(ModelCache::registerFillStrategy);
        ModelRegistrar.register();
        this.applyFieldCustomizers();
    }

    private void applyFieldCustomizers() {
        SimpleSpringUtils.getBeans(FieldCustomizer.class).stream()
            .sorted(Comparator.comparingInt(c -> getHierarchyDepth(c.getModelClass())))
            .forEach(FieldCustomizer::apply);
    }

    private static int getHierarchyDepth(Class<?> clazz) {
        int depth = 0;
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            depth++;
            c = c.getSuperclass();
        }
        return depth;
    }

}
