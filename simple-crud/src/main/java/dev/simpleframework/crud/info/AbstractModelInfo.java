package dev.simpleframework.crud.info;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.exception.ModelRegisterException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class AbstractModelInfo<T> implements ModelInfo<T> {

    /**
     * 模型类
     */
    private final Class<T> modelClass;
    /**
     * 模型名（表名）
     */
    private final String modelName;
    /**
     * 数据源类型
     */
    private final DatasourceType datasourceType;
    /**
     * 数据源名称
     */
    private final String datasourceName;
    /**
     * 主键
     */
    private ModelField<T> id;
    /**
     * 字段列表
     */
    private final Map<String, ModelField<T>> fields;

    protected AbstractModelInfo(Class<T> modelClass, String modelName, DatasourceType dsType, String dsName) {
        this.modelClass = modelClass;
        this.modelName = modelName;
        this.datasourceType = dsType;
        this.datasourceName = dsName;
        this.fields = new LinkedHashMap<>();
    }

    @Override
    public Class<T> modelClass() {
        return this.modelClass;
    }

    @Override
    public String name() {
        return this.modelName;
    }

    @Override
    public DatasourceType datasourceType() {
        return this.datasourceType;
    }

    @Override
    public String datasourceName() {
        return this.datasourceName;
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

    public void setId(String fieldName, Id.Type type) {
        if (fieldName == null) {
            this.id = null;
            return;
        }
        if (this.id != null) {
            throw new ModelRegisterException(this.name(), "Only support one id field");
        }
        ModelField<T> field = this.fields.get(fieldName);
        if (field == null) {
            throw new ModelRegisterException(this.name(), "No field named [" + fieldName + "]");
        }
        ((AbstractModelField<T>) field).changeToId(type);
        this.id = field;
    }

    protected Map<String, ModelField<T>> fields() {
        return this.fields;
    }

    protected void addFields(List<ModelField<T>> fields) {
        for (ModelField<T> field : fields) {
            this.addField(field);
        }
    }

    protected void addField(ModelField<T> field) {
        this.fields.put(field.fieldName(), field);
    }

}
