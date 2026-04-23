package dev.simpleframework.crud.core;

import dev.simpleframework.crud.info.AbstractModelInfo;
import dev.simpleframework.crud.util.ModelCache;
import dev.simpleframework.util.Functions;
import dev.simpleframework.util.SerializedFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 模型字段策略覆盖配置，用于在系统启动时统一声明模型字段的注解行为覆盖。
 * <p>
 * 注册为 Spring Bean 后，框架在启动时（{@code SimpleCrudAutoConfiguration.afterPropertiesSet}）自动应用所有 {@code ModelFieldConfig} bean
 *
 * <pre>{@code
 * @Bean
 * public ModelFieldConfig<User> userFieldConfig() {
 *     return ModelFieldConfig.of(User.class)
 *         .field(User::getId,          f -> f.id(Id.Type.SNOWFLAKE).updatable(false))
 *         .field(User::getCreatedTime, f -> f.autoFill(DataOperateDate.class).insertable(false).updatable(false))
 *         .field(User::getUpdatedTime, f -> f.autoFill(DataOperateDate.class));
 * }
 * }</pre>
 *
 * @param <T> 模型实体类型
 * @author loyayz (loyayz@foxmail.com)
 * @see FieldConfig
 * @see dev.simpleframework.crud.Models
 */
public class ModelFieldConfig<T> {

    private final Class<T> modelClass;
    private final List<FieldEntry> entries = new ArrayList<>();

    public static <T> ModelFieldConfig<T> of(Class<T> modelClass) {
        return new ModelFieldConfig<>(modelClass);
    }

    private ModelFieldConfig(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    /**
     * 覆盖指定字段的配置。
     *
     * @param fieldFunc 字段方法引用，如 {@code User::getId}
     * @param consumer  字段配置，通过 {@link FieldConfig} 链式调用设置覆盖项
     * @return this，支持链式调用
     */
    public ModelFieldConfig<T> field(SerializedFunction<T, ?> fieldFunc, Consumer<FieldConfig> consumer) {
        String fieldName = Functions.getLambdaFieldName(fieldFunc);
        FieldConfig config = new FieldConfig();
        consumer.accept(config);
        this.entries.add(new FieldEntry(fieldName, config));
        return this;
    }

    /**
     * 将所有字段配置应用到对应模型的 ModelInfo 中（由框架在启动时调用）。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void apply() {
        AbstractModelInfo info = (AbstractModelInfo) ModelCache.info(this.modelClass);
        for (FieldEntry entry : this.entries) {
            info.changeFieldConfig(entry.fieldName, entry.config);
        }
    }

    private record FieldEntry(String fieldName, FieldConfig config) {
    }

}
