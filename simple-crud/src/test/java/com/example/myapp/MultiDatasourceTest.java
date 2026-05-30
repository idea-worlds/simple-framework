package com.example.myapp;

import com.example.multids.model.SecondDsUserModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class MultiDatasourceTest {

    @AfterEach
    public void cleanupSecondDs() {
        try {
            new SecondDsUserModel().deleteByConditions(
                    dev.simpleframework.crud.core.QueryConditions.and()
                            .add("age", dev.simpleframework.crud.core.ConditionType.greater_equal, 0));
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testSecondDsModelShouldBeRegistered() {
        var info = dev.simpleframework.crud.util.ModelCache.info(SecondDsUserModel.class);
        assertNotNull(info);
        assertEquals("t_second", info.name());
        assertEquals("second", info.datasourceName());
    }

    @Test
    public void testSecondDsInsertAndFind() {
        var model = new SecondDsUserModel();
        model.setName("SecondUser");
        model.setAge(25);
        assertTrue(model.insert());
        assertNotNull(model.getId());

        var found = new SecondDsUserModel().findById(model.getId());
        assertNotNull(found);
        assertEquals("SecondUser", found.getName());
        assertEquals(25, found.getAge());
    }

    @Test
    public void testSecondDsUpdateAndDelete() {
        var model = new SecondDsUserModel();
        model.setName("UpdateMe");
        model.setAge(10);
        model.insert();
        Long id = model.getId();

        var update = new SecondDsUserModel();
        update.setId(id);
        update.setName("Updated");
        assertTrue(update.updateById());

        var found = new SecondDsUserModel().findById(id);
        assertEquals("Updated", found.getName());
        assertEquals(10, found.getAge());

        assertTrue(new SecondDsUserModel().deleteById(id));
        assertNull(new SecondDsUserModel().findById(id));
    }

    @Test
    public void testSecondDsListByConditions() {
        new SecondDsUserModel() {{ setName("A"); setAge(1); insert(); }};
        new SecondDsUserModel() {{ setName("B"); setAge(2); insert(); }};
        new SecondDsUserModel() {{ setName("A"); setAge(3); insert(); }};

        var config = dev.simpleframework.crud.core.QueryConfig.of().addCondition("name", "A");
        var list = new SecondDsUserModel().listByConditions(config);
        assertEquals(2, list.size());
    }

    @Test
    public void testTwoDatasourcesAreIsolated() {
        var primary = new com.example.myapp.model.UserModel();
        primary.setName("Primary");
        primary.setAge(1);
        primary.insert();

        var secondary = new SecondDsUserModel();
        secondary.setName("Secondary");
        secondary.setAge(2);
        secondary.insert();

        assertNotNull(new com.example.myapp.model.UserModel().findById(primary.getId()));
        assertNotNull(new SecondDsUserModel().findById(secondary.getId()));
        assertNull(new SecondDsUserModel().findById(primary.getId()));
    }

}
