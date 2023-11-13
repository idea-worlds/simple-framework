package dev.simpleframework.crud.util;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.info.clazz.ClassModelInfo;
import dev.simpleframework.util.Classes;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 模型注册表
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("all")
public class ModelRegistrar {
    private static final List<ModelRegistrar> WAIT_FOR_REGISTER = new CopyOnWriteArrayList<>();

    private final DatasourceType dsType;
    private final String dsName;
    private final Set<Class<?>> classes = new HashSet<>();

    public static ModelRegistrar newRegistrar(DatasourceType dsType, String dsName) {
        ModelRegistrar registrar = new ModelRegistrar(dsType, dsName);
        WAIT_FOR_REGISTER.add(registrar);
        return registrar;
    }

    public synchronized static void register() {
        List<ModelRegistrar> registrars = new ArrayList<>(WAIT_FOR_REGISTER);
        WAIT_FOR_REGISTER.clear();
        combine(registrars).forEach(ModelRegistrar::exec);
    }

    public void add(Class<?> modelClass) {
        this.classes.add(modelClass);
    }

    /**
     * 执行注册方法
     */
    private void exec() {
        this.classes.parallelStream()
                .filter(ModelRegistrar::canbeModel)
                .map(clazz -> new ClassModelInfo<>(clazz, this.dsType, this.dsName))
                .forEach(info -> {
                    Map<Class<?>, List<ModelMethod>> annotations = Classes.findAnnotations(info.modelClass(), ModelMethod.class);
                    if (!annotations.isEmpty()) {
                        annotations.forEach((methodClass, methods) -> {
                            methods.forEach(method -> Classes.newInstance(method.value()).register(info));
                        });
                        ModelCache.registerInfo(info);
                    }
                });
    }

    /**
     * 聚合同数据源的注册器
     */
    private static List<ModelRegistrar> combine(List<ModelRegistrar> registrars) {
        List<ModelRegistrar> result = new ArrayList<>();
        registrars.stream()
                .collect(Collectors.groupingBy(r -> r.dsType))
                .forEach((dsType, dsTypeRegistrars) -> {
                    dsTypeRegistrars.stream()
                            .collect(Collectors.groupingBy(r -> r.dsName))
                            .forEach((dsName, dsNameRegistrars) -> {
                                ModelRegistrar resultItem = new ModelRegistrar(dsType, dsName);
                                dsNameRegistrars.forEach(r -> resultItem.classes.addAll(r.classes));
                                result.add(resultItem);
                            });
                });
        registrars.clear();
        return result;
    }

    /**
     * 是否可作为模型类
     */
    private static boolean canbeModel(Class<?> clazz) {
        int classModifiers = clazz.getModifiers();
        if (Modifier.isFinal(classModifiers)
                || Modifier.isAbstract(classModifiers)
                || Modifier.isInterface(classModifiers)
                || clazz.getName().startsWith("java")) {
            return false;
        }
        return true;
    }

    private ModelRegistrar(DatasourceType dsType, String dsName) {
        this.dsType = dsType;
        this.dsName = dsName == null ? "" : dsName;
    }

}
