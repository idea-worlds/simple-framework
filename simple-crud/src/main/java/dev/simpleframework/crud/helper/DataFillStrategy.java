package dev.simpleframework.crud.helper;

import dev.simpleframework.crud.util.ModelCache;

import java.lang.annotation.Annotation;

/**
 * 数据填充策略，新增或修改数据前将自动替换模型字段的值
 * *
 * 需注册到 {@link ModelCache#registerFillStrategy(DataFillStrategy)}
 * 在 Spring 项目中带 @Component 的实现类将自动注册，非 Spring 请手动注册
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface DataFillStrategy {

    /**
     * 获取数据
     */
    <R> R get(Object param);

    /**
     * 数据填充注解
     */
    Class<?> support();

    /**
     * 注解配置转为策略参数
     *
     * @param annotation 注解
     * @return 参数。用于 {@link #get(Object)}
     */
    default Object toParam(Annotation annotation) {
        return null;
    }

    /**
     * 序号（越小约优先）
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 数据填充类型
     */
    default FillType type() {
        return FillType.ALWAYS;
    }

    enum FillType {
        /**
         * 值为 null 的时候才填充
         */
        NULL,
        /**
         * 直接替换原有值
         */
        ALWAYS
    }

}
