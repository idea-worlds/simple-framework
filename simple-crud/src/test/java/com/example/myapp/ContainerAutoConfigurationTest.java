package com.example.myapp;

import com.example.myapp.model.AutoIncrementModel;
import com.example.myapp.model.DataFillModel;
import com.example.myapp.model.UserModel;
import com.example.myapp.model.Uuid32Model;
import com.example.myapp.model.Uuid36Model;
import com.example.operator.model.UserPojo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ContainerAutoConfigurationTest {

    @Autowired
    private ApplicationContext ctx;

    @Test
    public void testContextLoadsAndAutoConfigurationWorks() {
        assertNotNull(ctx);
        assertNotNull(dev.simpleframework.util.SimpleSpringUtils.getApplicationContext());
    }

    @Test
    public void testBaseModelAutoRegisteredByModelScan() {
        var info = dev.simpleframework.crud.util.ModelCache.info(UserModel.class);
        assertNotNull(info);
        assertEquals("t_user", info.name());
    }

    @Test
    public void testAllBaseModelsAutoRegistered() {
        assertNotNull(dev.simpleframework.crud.util.ModelCache.info(Uuid32Model.class));
        assertNotNull(dev.simpleframework.crud.util.ModelCache.info(Uuid36Model.class));
        assertNotNull(dev.simpleframework.crud.util.ModelCache.info(AutoIncrementModel.class));
        assertNotNull(dev.simpleframework.crud.util.ModelCache.info(DataFillModel.class));
    }

    @Test
    public void testOperatorModelAutoRegisteredByModelScan() {
        assertNotNull(dev.simpleframework.crud.util.ModelCache.info(UserPojo.class));
    }

}
