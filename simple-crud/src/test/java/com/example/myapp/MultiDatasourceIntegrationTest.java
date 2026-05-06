package com.example.myapp;

import com.example.multids.model.SecondDsUserModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多数据源集成测试。
 * 主数据源（default）使用 UserModel → SYS_USER，
 * 第二数据源（second）使用 SecondDsUserModel → USER_INFO。
 * 验证两个数据源独立运作，互不干扰。
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class MultiDatasourceIntegrationTest {

    @Test
    public void testSecondDsModelShouldBeRegistered() {
        var info = dev.simpleframework.crud.util.ModelCache.info(SecondDsUserModel.class);
        assertNotNull(info, "SecondDsUserModel should be registered in ModelCache");
        assertEquals("user_info", info.name());
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
        assertEquals(10, found.getAge(), "age should remain unchanged");

        assertTrue(new SecondDsUserModel().deleteById(id));
        assertNull(new SecondDsUserModel().findById(id));
    }

    @Test
    public void testSecondDsListByConditions() {
        new SecondDsUserModel() {{ setName("A"); setAge(1); insert(); }};
        new SecondDsUserModel() {{ setName("B"); setAge(2); insert(); }};
        new SecondDsUserModel() {{ setName("A"); setAge(3); insert(); }};

        var config = dev.simpleframework.crud.core.QueryConfig.of()
                .addCondition("name", "A");
        var list = new SecondDsUserModel().listByConditions(config);
        assertEquals(2, list.size());
    }

    @Test
    public void testTwoDatasourcesAreIsolated() {
        // 主数据源写入
        var primary = new com.example.myapp.model.UserModel();
        primary.setName("Primary");
        primary.setAge(1);
        primary.insert();

        // 第二数据源写入同名
        var secondary = new SecondDsUserModel();
        secondary.setName("Secondary");
        secondary.setAge(2);
        secondary.insert();

        // 各自查询自己的数据，不交叉
        assertNotNull(new com.example.myapp.model.UserModel().findById(primary.getId()));
        assertNotNull(new SecondDsUserModel().findById(secondary.getId()));

        // 主数据源查不到第二数据源的数据
        assertNull(new SecondDsUserModel().findById(primary.getId()));
    }

}
