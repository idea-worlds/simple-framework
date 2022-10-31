package dev.simpleframework.crud;

import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.ModelConfiguration;
import dev.simpleframework.crud.exception.DatasourceNotRegisteredException;
import dev.simpleframework.crud.exception.FieldDefinitionException;
import dev.simpleframework.crud.exception.ModelNotRegisteredException;
import dev.simpleframework.crud.info.clazz.ClassModelInfo;
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
     * 注册模型
     * 不注册：抽象类、接口类、Map 和 Iterable 子类、java/javax 包下的类
     * 注册逻辑：从要注册的模型类一直往上找父类，然后从最后一个父类开始注册直至某个类注册成功，注册成功后子类都不再注册
     * eg: child -> parent -> grandparent
     * 若 grandparent 成功，则 parent 和 child 不再注册
     * 若 parent 成功，则 child 不再注册
     *
     * @param modelClass  要注册的模型类
     * @param superClass  父类
     * @param modelConfig 模型配置
     * @return 注册成功的模型类
     */
    public static Class<?> registerModel(Class<?> modelClass, Class<?> superClass, ModelConfiguration modelConfig) {
        modelClass = Classes.getTargetClassIfProxy(modelClass);
        if (modelClass == null || modelClass == Object.class || modelClass == superClass) {
            return null;
        }
        if (INFOS.containsKey(modelClass)) {
            return modelClass;
        }
        Class<?> registered = registerModel(modelClass.getSuperclass(), superClass, modelConfig);
        if (registered != null) {
            return registered;
        }
        int classModifiers = modelClass.getModifiers();
        if (Modifier.isFinal(classModifiers)
                || Modifier.isAbstract(classModifiers)
                || Modifier.isInterface(classModifiers)
                || Map.class.isAssignableFrom(modelClass)
                || Iterable.class.isAssignableFrom(modelClass)
                || modelClass.getName().startsWith("java.")
                || modelClass.getName().startsWith("javax.")) {
            return null;
        }
        ModelInfo<?> modelInfo = new ClassModelInfo<>(modelClass, modelConfig);
        if (modelInfo.getAllFields().isEmpty()) {
            throw new FieldDefinitionException(modelClass.toString(), "fields can not be empty");
        }
        INFOS.put(modelClass, modelInfo);
        return modelClass;
    }

    /**
     * 注册数据源
     *
     * @param type     数据源类型
     * @param provider 数据源提供者
     */
    public static void registerProvider(DatasourceType type, DatasourceProvider<?> provider) {
        PROVIDERS.compute(type, (k, old) -> {
            if (old == null) {
                return provider;
            }
            return provider.order() <= old.order() ? provider : old;
        });
    }

}
