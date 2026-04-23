package dev.simpleframework.crud.info;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.core.FieldConfig;
import dev.simpleframework.crud.helper.DataFillStrategy;
import dev.simpleframework.crud.helper.DataFillStrategy.FillType;
import dev.simpleframework.crud.util.ModelCache;

import java.lang.annotation.Annotation;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class AbstractModelField<T> implements ModelField<T> {
    /**
     * 表字段名
     */
    private String column;
    /**
     * 类字段名
     */
    private String fieldName;
    /**
     * 类字段类型
     */
    private Class<?> fieldType;
    /**
     * 类字段实际类型
     */
    private Class<?> fieldComponentType;
    /**
     * 是否可 insert
     */
    private boolean insertable;
    /**
     * 是否可 update
     */
    private boolean updatable;
    /**
     * 是否可 select
     */
    private boolean selectable;
    /**
     * 数据填充策略
     */
    private DataFillStrategy fillStrategy;
    private Object fillStrategyParam;

    @Override
    public void autoFillValue(T model) {
        if (this.fillStrategy == null) {
            return;
        }
        if (this.fillStrategy.type() == FillType.ALWAYS ||
                (this.fillStrategy.type() == FillType.NULL && this.getValue(model) == null)) {
            Object value = this.fillStrategy.get(this.fillStrategyParam);
            if (String.class.isAssignableFrom(this.fieldType) && !(value instanceof String)) {
                value = String.valueOf(value);
            }
            this.setValue(model, value);
        }
    }

    @Override
    public String columnName() {
        return this.column;
    }

    @Override
    public String fieldName() {
        return this.fieldName;
    }

    @Override
    public Class<?> fieldType() {
        return this.fieldType;
    }

    @Override
    public Class<?> fieldComponentType() {
        return this.fieldComponentType;
    }

    @Override
    public boolean insertable() {
        return this.insertable;
    }

    @Override
    public boolean updatable() {
        return this.updatable;
    }

    @Override
    public boolean selectable() {
        return this.selectable;
    }

    protected void setColumn(String column, String fieldName, Class<?> fieldType, Class<?> fieldComponentType) {
        this.column = column;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldComponentType = fieldComponentType == null ? fieldType.getComponentType() : fieldComponentType;
    }

    /**
     * 将 {@link FieldConfig} 中的非 null 配置项应用到此字段。
     * <p>
     * 仅覆盖 config 中显式设置的项，未设置（null）的项保持不变。
     */
    protected void config(FieldConfig config) {
        if (config.getColumnName() != null) {
            this.column = config.getColumnName();
        }
        if (config.getInsertable() != null) {
            this.insertable = config.getInsertable();
        }
        if (config.getUpdatable() != null) {
            this.updatable = config.getUpdatable();
        }
        if (config.getSelectable() != null) {
            this.selectable = config.getSelectable();
        }
        Annotation autoFill = config.getAutoFill();
        if (autoFill != null) {
            DataFillStrategy strategy = ModelCache.fillStrategy(autoFill.annotationType());
            this.fillStrategy = strategy;
            this.fillStrategyParam = strategy != null ? strategy.toParam(autoFill) : null;
        }
    }

    /**
     * 改为主键字段
     *
     * @param type 主键策略。默认 SNOWFLAKE
     */
    void changeToId(Id.Type type) {
        if (type == null) {
            type = Id.Type.SNOWFLAKE;
        }
        if (this.fillStrategy == null || this.fillStrategy.support() != Id.class) {
            this.fillStrategy = ModelCache.fillStrategy(Id.class);
            this.fillStrategyParam = type;
        }
    }

}
