package com.example.myapp;

import com.example.myapp.model.UserModel;
import com.example.operator.model.UserPojo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 容器启动与自动配置验证。
 * 验证 SimpleCoreAutoConfiguration → SimpleSpringUtils → SimpleCrudAutoConfiguration
 * → @ModelScan 全链路正确运行。
 *
 * 所有 CRUD 测试依赖这些验证通过——如果这里失败，"Class is not registered"。
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ContainerAutoConfigurationTest {

    @Autowired
    private ApplicationContext ctx;

    /** 验证 Spring 容器正常启动且 SimpleSpringUtils 被 SimpleCoreAutoConfiguration 初始化 */
    @Test
    public void testContextLoadsAndAutoConfigurationWorks() {
        assertNotNull(ctx);
        assertNotNull(dev.simpleframework.util.SimpleSpringUtils.getApplicationContext());
    }

    /** 验证 @ModelScan(basePackages="com.example.myapp.model") → UserModel 已注册到 ModelCache */
    @Test
    public void testBaseModelAutoRegisteredByModelScan() {
        var info = dev.simpleframework.crud.util.ModelCache.info(UserModel.class);
        assertNotNull(info);
        assertEquals("sys_user", info.name());
    }

    /** 验证 @ModelScan(basePackages="com.example.operator.model", operatorClass=Models.class) → UserPojo 已注册 */
    @Test
    public void testOperatorModelAutoRegisteredByModelScan() {
        assertNotNull(dev.simpleframework.crud.util.ModelCache.info(UserPojo.class));
    }

}
