package com.example.myapp;

import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.spring.ModelScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ModelScan(basePackages = "com.example.myapp.model")
@ModelScan(
        basePackages = "com.example.operator.model",
        superClass = Object.class,
        operatorClass = Models.class
)
@ModelScan(
        basePackages = "com.example.multids.model",
        datasourceName = "second"
)
public class TestApplication {
}
