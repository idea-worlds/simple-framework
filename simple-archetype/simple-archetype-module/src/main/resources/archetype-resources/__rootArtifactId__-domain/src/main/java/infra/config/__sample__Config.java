package ${package}.infra.config;

import dev.simpleframework.crud.spring.ModelScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "${package}.infra.data.mapper")
@ModelScan(basePackages = "${package}.infra.data")
public class ${sample}Config {

}
