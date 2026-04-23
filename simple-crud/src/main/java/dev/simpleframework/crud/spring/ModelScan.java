package dev.simpleframework.crud.spring;

import dev.simpleframework.crud.BaseModel;
import dev.simpleframework.crud.ModelOperator;
import dev.simpleframework.crud.core.DatasourceType;
import dev.simpleframework.crud.helper.DatasourceProvider;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * simple-crud 将扫描指定包下继承 superClass() 的类注册到 Models
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Import(ModelScannerRegistrar.class)
@Repeatable(ModelScans.class)
public @interface ModelScan {

    /**
     * Alias for basePackages
     */
    String[] value() default {};

    /**
     * Alias for value
     * 要扫描的包名，未指定包名时默认扫描添加 @ModelScan 注解的类所在的包
     * 例：
     * - package com.sample.simple;
     * - @ModelScan
     * - public class Test {}
     * 会扫描 com.sample.simple
     */
    String[] basePackages() default {};

    /**
     * 模型类的父类
     */
    Class<?> superClass() default BaseModel.class;

    /**
     * 操作类，决定为扫描到的实体类注册哪些 CRUD SQL。
     * <p>
     * 框架读取 operatorClass 接口层级上的 {@code @ModelMethod} 注解来注册 SQL。
     * 默认值 {@link ModelOperator} 表示不注册任何 SQL（只建立模型信息，不可操作）。
     * 使用 {@link dev.simpleframework.crud.Models} 可注册全套 CRUD SQL。
     * <p>
     * 仅对无 {@code @ModelMethod} 注解的普通实体类生效；继承 {@link BaseModel} 的类
     * 仍按其自身声明的 {@code @ModelMethod} 注解注册。
     */
    Class<? extends ModelOperator> operatorClass() default ModelOperator.class;

    /**
     * 模型类型
     * 用于 ${@link DatasourceProvider} 获取数据源
     */
    DatasourceType datasourceType() default DatasourceType.Mybatis;

    /**
     * 数据源名
     * 用于 ${@link DatasourceProvider} 获取数据源
     */
    String datasourceName() default "";

}
