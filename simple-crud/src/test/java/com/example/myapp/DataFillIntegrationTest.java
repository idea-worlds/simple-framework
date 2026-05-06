package com.example.myapp;

import com.example.myapp.model.DateModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataFillStrategy 自动填充集成测试。
 * 验证 @DataOperateDate 时间填充和 Snowflake ID 生成在真实 SQL 执行中生效。
 * DateModel 继承 SimpleModel，预置 id、createUser、createdTime、updatedTime 四个字段。
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class DataFillIntegrationTest {

    @Test
    public void testInsertShouldAutoFillTimeFields() {
        var model = new DateModel();
        model.setName("TimeTest");
        assertTrue(model.insert());
        assertNotNull(model.getCreatedTime());
        assertNotNull(model.getUpdatedTime());
        assertEquals(model.getCreatedTime(), model.getUpdatedTime());
    }

    @Test
    public void testUpdateShouldRefreshUpdatedTimeOnly() {
        var model = new DateModel();
        model.setName("TimeTest");
        model.insert();
        Date oldCreated = model.getCreatedTime();
        Date oldUpdated = model.getUpdatedTime();

        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        var update = new DateModel();
        update.setId(model.getId());
        update.setName("Updated");
        update.updateById();

        var found = new DateModel().findById(model.getId());
        assertEquals(oldCreated, found.getCreatedTime(), "createdTime should not change");
        assertTrue(found.getUpdatedTime().compareTo(oldUpdated) >= 0,
                "updatedTime should be >= old value");
    }

    @Test
    public void testInsertShouldAutoGenerateSnowflakeId() {
        var a = new DateModel(); a.setName("A"); a.insert();
        var b = new DateModel(); b.setName("B"); b.insert();
        assertNotNull(a.getId());
        assertNotNull(b.getId());
        assertTrue(a.getId() < b.getId(), "Snowflake IDs should be monotonically increasing");
    }

    @Test
    public void testCreateUserShouldBeNullWithoutCustomStrategy() {
        var model = new DateModel();
        model.setName("NoUser");
        model.insert();
        assertNull(model.getCreateUser(),
                "createUser is null because no custom DataFillStrategy registered");
    }

}
