package dev.simpleframework.crud.util;

import dev.simpleframework.crud.DynamicModel;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.exception.ModelExecuteException;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.helper.DataFillStrategy;
import dev.simpleframework.crud.helper.DatasourceProvider;
import dev.simpleframework.util.Classes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("all")
public final class ModelCache {
    private static final Map<Class, ModelInfo> INFOS = new ConcurrentHashMap<>();
    private static final Map<DatasourceType, DatasourceProvider<?>> PROVIDERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, DataFillStrategy> FILL_STRATEGY = new ConcurrentHashMap<>();

    /**
     * 获取模型类对应的表信息
     *
     * @param modelOrClass 模型实例或模型类
     * @return 表信息
     */
    public static <T> ModelInfo<T> info(Object modelOrClass) {
        if (modelOrClass instanceof DynamicModel) {
            return (ModelInfo<T>) ((DynamicModel) modelOrClass).info();
        }
        Class<?> targetClass = Classes.getTargetClassIfProxy(modelOrClass.getClass());
        if (targetClass == Class.class) {
            targetClass = Classes.getTargetClassIfProxy((Class) modelOrClass);
        }
        ModelInfo result = INFOS.get(targetClass);
        if (result == null) {
            result = Classes.findInfo(targetClass, c -> INFOS.get(c));
        }
        if (result == null) {
            throw new ModelExecuteException(targetClass, "Class is not registered");
        }
        return result;
    }

    /**
     * 获取数据源
     *
     * @param type 数据源类型
     * @return 数据源
     */
    public static <T> DatasourceProvider<T> provider(DatasourceType type) {
        DatasourceProvider<T> provider;
        try {
            provider = (DatasourceProvider<T>) PROVIDERS.get(type);
        } catch (ClassCastException e) {
            throw new ModelRegisterException(type);
        }
        if (provider == null) {
            throw new ModelRegisterException(type);
        }
        return provider;
    }

    /**
     * 获取数据填充策略
     *
     * @param annotion 数据填充注解
     * @return 数据填充策略
     */
    public static DataFillStrategy fillStrategy(Class<?> annotion) {
        return FILL_STRATEGY.get(annotion);
    }

    /**
     * 注册模型信息
     */
    public static void registerInfo(ModelInfo<?> info) {
        Class<?> modelClass = info.modelClass();
        if (info.getAllFields().isEmpty()) {
            throw new ModelRegisterException(modelClass, "Fields can not be empty");
        }
        if (INFOS.put(modelClass, info) != null) {
            throw new ModelRegisterException(modelClass, "Class has been registered");
        }
    }

    /**
     * 注册数据源
     *
     * @param provider 数据源提供者
     */
    public static void registerProvider(DatasourceProvider<?> provider) {
        PROVIDERS.compute(provider.support(), (k, old) -> {
            if (old == null) {
                return provider;
            }
            return provider.order() <= old.order() ? provider : old;
        });
    }

    /**
     * 注册数据填充策略
     *
     * @param strategy 数据填充策略
     */
    public static void registerFillStrategy(DataFillStrategy strategy) {
        FILL_STRATEGY.compute(strategy.support(), (k, old) -> {
            if (old == null) {
                return strategy;
            }
            return strategy.order() <= old.order() ? strategy : old;
        });
    }

}
