package dev.simpleframework.util;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SimpleCoreAutoConfiguration {

    @Bean
    public static SimpleSpringUtils simpleSpringUtils() {
        return new SimpleSpringUtils();
    }

}
