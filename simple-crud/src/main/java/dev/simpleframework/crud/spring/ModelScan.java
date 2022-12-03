package dev.simpleframework.crud.spring;

import dev.simpleframework.crud.BaseModel;
import dev.simpleframework.crud.DatasourceProvider;
import dev.simpleframework.crud.core.DatasourceType;
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
