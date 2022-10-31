package dev.simpleframework.crud.info;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.core.ModelConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class AbstractModelInfo<T> implements ModelInfo<T> {

    /**
     * 模型类
     */
    private final Class<T> modelClass;
    /**
     * 模型配置
     */
    private final ModelConfiguration config;
    /**
     * 模型名（表名）
     */
    private final String modelName;
    /**
     * 主键
     */
    private ModelId<T> id;
    /**
     * 字段列表
     */
    private final Map<String, ModelField<T>> fields;

    protected AbstractModelInfo(Class<T> modelClass, ModelConfiguration config, String modelName) {
        this.modelClass = modelClass;
        this.config = config;
        this.modelName = modelName;
        this.fields = new LinkedHashMap<>();
    }

    @Override
    public Class<T> modelClass() {
        return this.modelClass;
    }

    @Override
    public ModelConfiguration config() {
        return this.config;
    }

    @Override
    public String name() {
        return this.modelName;
    }

    @Override
    public ModelField<T> id() {
        return this.id;
    }

    @Override
    public List<ModelField<T>> getAllFields() {
        return new ArrayList<>(this.fields.values());
    }

    @Override
    public ModelField<T> getField(String fieldName) {
        return this.fields.get(fieldName);
    }

    protected void setId(ModelId<T> id) {
        this.id = id;
    }

    protected Map<String, ModelField<T>> fields() {
        return this.fields;
    }

    protected void addField(List<ModelField<T>> fields) {
        for (ModelField<T> field : fields) {
            this.addField(field);
        }
    }

    protected void addField(ModelField<T> field) {
        this.fields.put(field.fieldName(), field);
    }

}
