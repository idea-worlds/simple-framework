package dev.simpleframework.crud.info;

import dev.simpleframework.crud.ModelField;
import dev.simpleframework.crud.core.ModelIdStrategy;
import dev.simpleframework.util.Snowflake;
import lombok.SneakyThrows;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class ModelId<T> extends AbstractModelField<T> {

    private final ModelField<T> field;
    /**
     * 主键生成器
     */
    private final Supplier<Object> generator;

    private final boolean isString;

    public ModelId(ModelField<T> field) {
        this(field, ModelIdStrategy.SNOWFLAKE);
    }

    public ModelId(ModelField<T> field, ModelIdStrategy strategy) {
        this(field, buildIdGenerator(strategy));
    }

    public ModelId(ModelField<T> field, Supplier<Object> generator) {
        this.generator = generator;
        AbstractModelField<T> f = (AbstractModelField<T>) field;
        // 自增字段不可新增
        f.setInsertable(generator != null);
        // 主键字段不可修改
        f.setUpdatable(false);
        // 主键字段必可查
        f.setSelectable(true);
        this.field = f;
        this.isString = String.class.isAssignableFrom(this.field.fieldType());
    }

    @Override
    public <R> R getValue(T model) {
        return this.field.getValue(model);
    }

    @Override
    @SneakyThrows
    public void setValue(T model, Object value) {
        if (value != null) {
            this.field.setValue(model, value);
            return;
        }
        if (this.generator == null || this.getValue(model) != null) {
            return;
        }
        value = this.generator.get();
        if (value != null && this.isString) {
            value = String.valueOf(value);
        }
        this.field.setValue(model, value);
    }

    @Override
    public String columnName() {
        return this.field.columnName();
    }

    @Override
    public String fieldName() {
        return this.field.fieldName();
    }

    @Override
    public Class<?> fieldType() {
        return this.field.fieldType();
    }

    @Override
    public boolean insertable() {
        return this.field.insertable();
    }

    @Override
    public boolean updatable() {
        return this.field.updatable();
    }

    /**
     * 获取表主键策略
     */
    private static Supplier<Object> buildIdGenerator(ModelIdStrategy strategy) {
        if (strategy == ModelIdStrategy.AUTO_INCREMENT) {
            return null;
        }
        if (strategy == ModelIdStrategy.UUID32) {
            return () -> UUID.randomUUID().toString().replace("-", "");
        }
        if (strategy == ModelIdStrategy.UUID36) {
            return () -> UUID.randomUUID().toString();
        }
        if (strategy == ModelIdStrategy.SNOWFLAKE) {
            return Snowflake.DEFAULT::nextId;
        }
        return null;
    }

}
