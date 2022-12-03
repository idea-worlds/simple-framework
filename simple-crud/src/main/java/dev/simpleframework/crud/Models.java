package dev.simpleframework.crud;

import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.exception.DatasourceNotRegisteredException;
import dev.simpleframework.crud.exception.FieldDefinitionException;
import dev.simpleframework.crud.exception.ModelNotRegisteredException;
import dev.simpleframework.crud.info.clazz.ClassModelInfo;
import dev.simpleframework.crud.strategy.DataFillStrategy;
import dev.simpleframework.util.Classes;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("all")
public final class Models {
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
            result = INFOS.get(targetClass);
        }
        if (result == null) {
            result = Classes.getInfoFromSuperclass(targetClass, c -> INFOS.get(c));
        }
        if (result == null) {
            throw new ModelNotRegisteredException(targetClass);
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
            throw new DatasourceNotRegisteredException(type);
        }
        if (provider == null) {
            throw new DatasourceNotRegisteredException(type);
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
     * 注册模型
     * 不注册：抽象类、接口类、Map 和 Iterable 子类、java 包下的类
     * 注册逻辑：从要注册的模型类一直往上找父类，然后从最后一个父类开始注册直至某个类注册成功，注册成功后子类都不再注册
     * eg: child -> parent -> grandparent
     * 若 grandparent 成功，则 parent 和 child 不再注册
     * 若 parent 成功，则 child 不再注册
     *
     * @param modelClass 要注册的模型类
     * @param superClass 父类
     * @param dsType     数据源类型
     * @param dsName     数据源名称
     * @return 注册成功的模型类
     */
    public static Class<?> registerModel(Class<?> modelClass, Class<?> superClass, DatasourceType dsType, String dsName) {
        modelClass = Classes.getTargetClassIfProxy(modelClass);
        if (modelClass == null || modelClass == Object.class || modelClass == superClass) {
            return null;
        }
        if (INFOS.containsKey(modelClass)) {
            return modelClass;
        }
        Class<?> registered = registerModel(modelClass.getSuperclass(), superClass, dsType, dsName);
        if (registered != null) {
            return registered;
        }
        int classModifiers = modelClass.getModifiers();
        if (Modifier.isFinal(classModifiers)
                || Modifier.isAbstract(classModifiers)
                || Modifier.isInterface(classModifiers)
                || Map.class.isAssignableFrom(modelClass)
                || Iterable.class.isAssignableFrom(modelClass)
                || modelClass.getName().startsWith("java")) {
            return null;
        }
        ModelInfo<?> modelInfo = new ClassModelInfo<>(modelClass, dsType, dsName);
        if (modelInfo.getAllFields().isEmpty()) {
            throw new FieldDefinitionException(modelClass.toString(), "fields can not be empty");
        }
        INFOS.put(modelClass, modelInfo);
        return modelClass;
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
