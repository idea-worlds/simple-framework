package dev.simpleframework.crud.info.dynamic;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.info.AbstractModelInfo;
import dev.simpleframework.util.Strings;

import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class DynamicModelInfo extends AbstractModelInfo<Map<String, Object>> {

    private final String methodNamespace;

    public DynamicModelInfo(String modelName, DatasourceType datasourceType) {
        this(modelName, datasourceType, "");
    }

    @SuppressWarnings("unchecked")
    public DynamicModelInfo(String modelName, DatasourceType datasourceType, String datasourceName) {
        super(
                (Class<Map<String, Object>>) (Object) Map.class,
                modelName, datasourceType, datasourceName
        );
        this.methodNamespace = modelName + "_" + Math.abs(this.hashCode());
    }

    @Override
    public String methodNamespace() {
        return this.methodNamespace;
    }

    @Override
    public boolean dynamic() {
        return true;
    }

    public DynamicModelInfo setId(String fieldName) {
        super.setId(fieldName, Id.Type.SNOWFLAKE);
        return this;
    }

    public DynamicModelInfo addField(String fieldName) {
        return this.addField(fieldName, null, String.class);
    }

    public DynamicModelInfo addField(String fieldName, String column) {
        return this.addField(fieldName, column, String.class);
    }

    public DynamicModelInfo addField(String fieldName, Class<?> fieldType) {
        return this.addField(fieldName, null, fieldType);
    }

    public DynamicModelInfo addField(String fieldName, String column, Class<?> fieldType) {
        if (Strings.isBlank(column)) {
            column = Strings.camelToUnderline(fieldName);
        }
        DynamicModelField field = new DynamicModelField(fieldName, column, fieldType);
        return this.addField(field);
    }

    public DynamicModelInfo addField(DynamicModelField field) {
        super.addField(field);
        return this;
    }

    public DynamicModelInfo removeField(String fieldName) {
        if (fieldName == null) {
            return this;
        }
        if (this.isIdField(fieldName)) {
            super.setId(null, null);
        } else {
            super.fields().remove(fieldName);
            super.invalidateFieldCache();
        }
        return this;
    }

    public DynamicModelInfo removeAllFields() {
        super.setId(null, null);
        super.fields().clear();
        super.invalidateFieldCache();
        return this;
    }

    private boolean isIdField(String fieldName) {
        ModelField<Map<String, Object>> modelId = super.id();
        if (modelId == null) {
            return false;
        }
        return modelId.fieldName().equals(fieldName);
    }

}
