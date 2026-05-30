package com.example.myapp;

import com.example.myapp.model.DataFillModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class DataFillTest {

    @Test
    public void testInsertShouldAutoFillTimeFields() {
        var model = new DataFillModel();
        model.setName("TimeTest");
        Date beforeInsert = new Date();
        assertTrue(model.insert());
        Date afterInsert = new Date();

        var found = new DataFillModel().findById(model.getId());
        assertNotNull(found.getCreatedTime());
        assertNotNull(found.getUpdatedTime());
        assertFalse(found.getCreatedTime().before(beforeInsert));
        assertFalse(found.getCreatedTime().after(afterInsert));
        assertFalse(found.getUpdatedTime().before(beforeInsert));
        assertFalse(found.getUpdatedTime().after(afterInsert));
    }

    @Test
    public void testUpdateShouldRefreshUpdatedTimeOnly() {
        var model = new DataFillModel();
        model.setName("TimeTest");
        model.insert();
        Long id = model.getId();
        Date oldCreated = model.getCreatedTime();
        Date oldUpdated = model.getUpdatedTime();

        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        var update = new DataFillModel();
        update.setId(id);
        update.setName("Updated");
        update.updateById();

        var found = new DataFillModel().findById(id);
        assertEquals(oldCreated, found.getCreatedTime());
        assertTrue(found.getUpdatedTime().compareTo(oldUpdated) > 0);
    }

    @Test
    public void testInsertShouldAutoGenerateSnowflakeId() {
        var a = new DataFillModel(); a.setName("A"); a.insert();
        var b = new DataFillModel(); b.setName("B"); b.insert();
        assertTrue(a.getId() > 0);
        assertTrue(b.getId() > a.getId());
    }

    @Test
    public void testCreateUserShouldBeNullWithoutCustomStrategy() {
        var model = new DataFillModel();
        model.setName("NoUser");
        model.insert();
        assertNull(model.getCreateUser());
    }

    @Test
    public void testInsertBatchShouldAlsoFillTimeFields() {
        var a = new DataFillModel(); a.setName("A");
        var b = new DataFillModel(); b.setName("B");
        assertTrue(new DataFillModel().insertBatch(List.of(a, b)));
        assertNotNull(a.getCreatedTime());
        assertNotNull(a.getUpdatedTime());
        assertNotNull(b.getCreatedTime());
        assertNotNull(b.getUpdatedTime());
    }

}
