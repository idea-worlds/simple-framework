package dev.simpleframework.crud.info;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.ModelInfo;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.core.FieldOptions;
import dev.simpleframework.crud.exception.ModelRegisterException;
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
    // 字段列表缓存，addField/setId 时置 null 失效
    private List<ModelField<T>> cachedAllFields;
    private List<ModelField<T>> cachedInsertFields;
    private List<ModelField<T>> cachedUpdateFields;
    private List<ModelField<T>> cachedSelectFields;

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
        if (this.cachedAllFields == null) {
            this.cachedAllFields = List.copyOf(this.fields.values());
        }
        return this.cachedAllFields;
    }

    @Override
    public List<ModelField<T>> getInsertFields() {
        if (this.cachedInsertFields == null) {
            this.cachedInsertFields = this.getAllFields().stream()
                    .filter(ModelField::insertable)
                    .toList();
        }
        return this.cachedInsertFields;
    }

    @Override
    public List<ModelField<T>> getUpdateFields() {
        if (this.cachedUpdateFields == null) {
            this.cachedUpdateFields = this.getAllFields().stream()
                    .filter(ModelField::updatable)
                    .toList();
        }
        return this.cachedUpdateFields;
    }

    @Override
    public List<ModelField<T>> getSelectFields() {
        if (this.cachedSelectFields == null) {
            this.cachedSelectFields = this.getAllFields().stream()
                    .filter(ModelField::selectable)
                    .toList();
        }
        return this.cachedSelectFields;
    }

    @Override
    public ModelField<T> getField(String fieldName) {
        return this.fields.get(fieldName);
    }

    public void setId(String fieldName, Id.Type type) {
        if (fieldName == null) {
            this.id = null;
            invalidateFieldCache();
            return;
        }
        ModelField<T> field = this.fields.get(fieldName);
        if (field == null) {
            throw new ModelRegisterException(this.name(), "No field named [" + fieldName + "]");
        }
        if (this.id != null && this.id != field) {
            throw new ModelRegisterException(this.name(), "Only support one id field");
        }
        ((AbstractModelField<T>) field).changeToId(type);
        this.id = field;
        invalidateFieldCache();
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
        invalidateFieldCache();
    }

    /**
     * 覆盖指定字段的配置（供 Operator 场景按需修改字段策略）。
     * <p>
     * 修改后会自动使字段列表缓存失效，下次调用 getXxxFields() 时重新计算。
     *
     * @param fieldName 字段名（Java 类中的字段名，非列名）
     * @param config    字段覆盖配置
     */
    public void changeFieldOptions(String fieldName, FieldOptions config) {
        ModelField<T> raw = this.fields.get(fieldName);
        if (!(raw instanceof AbstractModelField<T> field)) {
            return;
        }
        field.config(config);
        if (config.getIdType() != null) {
            setId(fieldName, config.getIdType());
        }
        invalidateFieldCache();
    }

    protected void invalidateFieldCache() {
        this.cachedAllFields = null;
        this.cachedInsertFields = null;
        this.cachedUpdateFields = null;
        this.cachedSelectFields = null;
    }

}
