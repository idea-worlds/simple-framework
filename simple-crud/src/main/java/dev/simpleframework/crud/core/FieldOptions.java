package dev.simpleframework.crud.core;

import dev.simpleframework.crud.annotation.Id;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 * 单个字段的策略选项，用于覆盖 POJO 字段的注解行为。
 * 通过 {@link FieldCustomizer#field} 方法传入链式调用设置选项，
 * 在系统启动阶段统一应用于全局模型信息，不影响 POJO 原始定义。
 *
 * @author loyayz (loyayz@foxmail.com)
 * @see FieldCustomizer
 */
@Getter
public class FieldOptions {

    private String columnName;
    private Boolean insertable;
    private Boolean updatable;
    private Boolean selectable;
    private Id.Type idType;
    private Annotation autoFill;

    /**
     * 覆盖列名，等价于 {@code @Column(name = "...")}
     */
    public FieldOptions name(String columnName) {
        this.columnName = columnName;
        return this;
    }

    /**
     * 设置主键策略，等价于 {@code @Id(type = ...)}
     */
    public FieldOptions id(Id.Type type) {
        this.idType = type;
        return this;
    }

    /**
     * 设置字段是否参与 INSERT，等价于 {@code @Column(insertable = ...)}
     */
    public FieldOptions insertable(boolean insertable) {
        this.insertable = insertable;
        return this;
    }

    /**
     * 设置字段是否参与 UPDATE，等价于 {@code @Column(updatable = ...)}
     */
    public FieldOptions updatable(boolean updatable) {
        this.updatable = updatable;
        return this;
    }

    /**
     * 设置字段是否参与 SELECT，等价于 {@code @Column(selectable = ...)}
     */
    public FieldOptions selectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    /**
     * 指定自动填充注解类型，等价于在字段上声明对应注解。
     * <p>
     * 常用值：{@code DataOperateDate.class}（填充当前时间）、{@code DataOperateUser.class}（填充当前用户）。
     * 框架会从 {@link dev.simpleframework.crud.util.ModelCache} 中查找对应的 {@link dev.simpleframework.crud.helper.DataFillStrategy}，
     * 并通过 {@link dev.simpleframework.crud.helper.DataFillStrategy#toParam(Annotation)} 提取策略参数。
     * <p>
     * 注解无属性时使用此方法；有属性时请使用 {@link #autoFill(Annotation)}。
     */
    public FieldOptions autoFill(Class<? extends Annotation> annotationType) {
        Annotation annotation = AnnotationUtils.synthesizeAnnotation(annotationType);
        return this.autoFill(annotation);
    }

    /**
     * 指定自动填充注解实例，等价于在字段上声明对应注解。
     * <p>
     * 用于传入带属性的注解实例，框架通过 {@link dev.simpleframework.crud.helper.DataFillStrategy#toParam(Annotation)} 提取策略参数。
     */
    public FieldOptions autoFill(Annotation annotation) {
        this.autoFill = annotation;
        return this;
    }

}
