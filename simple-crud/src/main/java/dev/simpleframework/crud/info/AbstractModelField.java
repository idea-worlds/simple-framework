package dev.simpleframework.crud.info;

import dev.simpleframework.crud.ModelField;

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

    protected void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }

    protected void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    protected void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    protected void setColumn(String column, String fieldName, Class<?> fieldType, Class<?> fieldComponentType) {
        this.column = column;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldComponentType = fieldComponentType == null ? fieldType.getComponentType() : fieldComponentType;
    }

}
