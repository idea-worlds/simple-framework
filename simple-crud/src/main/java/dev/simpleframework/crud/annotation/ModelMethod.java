package dev.simpleframework.crud.annotation;

import dev.simpleframework.crud.method.ModelMethodDefinition;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 模型方法
 *
 * @author loyayz (loyayz@foxmail.com)
 * @see ModelMethodDefinition
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface ModelMethod {

    Class<? extends ModelMethodDefinition> value();

}
