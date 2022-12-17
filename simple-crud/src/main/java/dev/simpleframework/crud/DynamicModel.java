package dev.simpleframework.crud;

import dev.simpleframework.crud.exception.ModelExecuteException;
import dev.simpleframework.crud.exception.ModelRegisterException;
import dev.simpleframework.crud.info.dynamic.DynamicModelInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态模型
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class DynamicModel implements BaseModel<Map<String, Object>> {
    private static final Map<String, DynamicModelInfo> INFOS = new ConcurrentHashMap<>();

    private String modelName;
    private DynamicModelInfo info;

    /**
     * 注册模型
     */
    public static void register(DynamicModelInfo info) {
        if (info == null) {
            return;
        }
        if (info.getAllFields().isEmpty()) {
            throw new ModelRegisterException(info.name(), "Fields can not be empty");
        }
        INFOS.put(info.name(), info);
    }

    /**
     * 注销注册的模型
     *
     * @param modelName 模型名
     */
    public static void removeRegistered(String modelName) {
        INFOS.remove(modelName);
    }

    public static DynamicModel of(String modelName) {
        DynamicModel model = new DynamicModel();
        model.modelName = modelName;
        return model;
    }

    public static DynamicModel of(DynamicModelInfo info) {
        DynamicModel model = new DynamicModel();
        model.modelName = info.name();
        model.info = info;
        return model;
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
