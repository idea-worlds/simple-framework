package dev.simpleframework.crud.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * simple-crud 根据配置扫描指定的类并注册到 Models
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Retention(RUNTIME)
@Target({TYPE})
@Documented
@Import(ModelScannerRegistrar.RepeatingRegistrar.class)
public @interface ModelScans {

    ModelScan[] value();

}
