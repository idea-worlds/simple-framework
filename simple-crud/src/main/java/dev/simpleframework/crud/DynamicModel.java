package dev.simpleframework.crud;

import dev.simpleframework.crud.annotation.ModelMethod;
import dev.simpleframework.crud.exception.ModelExecuteException;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.info.dynamic.DynamicModelInfo;
import dev.simpleframework.util.Classes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态模型：运行时注册表结构，无需编译期实体类。
 * 继承 HashMap，自身就是数据载体，insert/update 时字段值直接通过 put/get 存取。
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class DynamicModel extends HashMap<String, Object> implements BaseModel<Map<String, Object>> {
    private static final Map<String, DynamicModelInfo> INFOS = new ConcurrentHashMap<>();

    private final String modelName;
    private DynamicModelInfo info;

    private DynamicModel(String modelName) {
        this.modelName = modelName;
    }

    private DynamicModel(String modelName, DynamicModelInfo info) {
        this.modelName = modelName;
        this.info = info;
    }

    /**
     * 注册模型，同时读取 DynamicModel 接口上的 @ModelMethod 注解注册 MyBatis SQL。
     */
    public static void register(DynamicModelInfo info) {
        if (info == null) {
            return;
        }
        if (info.getAllFields().isEmpty()) {
            throw new ModelRegisterException(info.name(), "Fields can not be empty");
        }
        INFOS.put(info.name(), info);

        // 读取 DynamicModel 接口上的 @ModelMethod 注解，注册对应的 MyBatis MappedStatement
        Map<Class<?>, java.util.List<ModelMethod>> annotations =
                Classes.findAnnotations(DynamicModel.class, ModelMethod.class);
        annotations.forEach((methodClass, methods) -> {
            methods.forEach(method -> Classes.newInstance(method.value()).register(info));
        });
    }

    /**
     * 注销注册的模型
     */
    public static void removeRegistered(String modelName) {
        INFOS.remove(modelName);
    }

    public static DynamicModel of(String modelName) {
        return new DynamicModel(modelName);
    }

    public static DynamicModel of(DynamicModelInfo info) {
        return new DynamicModel(info.name(), info);
    }

    /**
     * 注册模型
     */
    public DynamicModel register() {
        register(this.info);
        return this;
    }

    /**
     * 获取模型名
     */
    public String modelName() {
        return this.modelName;
    }

    /**
     * 获取模型信息
     */
    public DynamicModelInfo info() {
        if (this.info == null) {
            this.info = INFOS.get(this.modelName);
            if (this.info == null) {
                throw new ModelExecuteException("Model is not registered: " + this.modelName);
            }
        }
        return this.info;
    }

}
