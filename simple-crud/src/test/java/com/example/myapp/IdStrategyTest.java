package com.example.myapp;

import com.example.myapp.model.AutoIncrementModel;
import com.example.myapp.model.UserModel;
import com.example.myapp.model.Uuid32Model;
import com.example.myapp.model.Uuid36Model;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class IdStrategyTest {

    // ==================== SNOWFLAKE ====================

    @Test
    public void testSnowflakeInsertShouldGenerateLongId() {
        var u = new UserModel(); u.setName("Sf"); u.setAge(1);
        assertTrue(u.insert());
        assertNotNull(u.getId());
        assertTrue(u.getId() > 0);
        assertEquals("Sf", new UserModel().findById(u.getId()).getName());
    }

    @Test
    public void testSnowflakeInsertBatchShouldGenerateDistinctIds() {
        var a = new UserModel(); a.setName("A"); a.setAge(1);
        var b = new UserModel(); b.setName("B"); b.setAge(2);
        var c = new UserModel(); c.setName("C"); c.setAge(3);
        assertTrue(new UserModel().insertBatch(List.of(a, b, c)));

        assertNotNull(a.getId());
        assertNotNull(b.getId());
        assertNotNull(c.getId());
        assertTrue(a.getId() < b.getId());
        assertTrue(b.getId() < c.getId());

        assertNotNull(new UserModel().findById(a.getId()));
        assertNotNull(new UserModel().findById(b.getId()));
        assertNotNull(new UserModel().findById(c.getId()));
    }

    // ==================== UUID32 ====================

    @Test
    public void testUuid32InsertShouldGenerate32CharHex() {
        var u = new Uuid32Model(); u.setName("U32"); u.setAge(1);
        assertTrue(u.insert());
        String id = u.getId();
        assertNotNull(id);
        assertEquals(32, id.length());
        assertFalse(id.contains("-"));
        assertEquals("U32", new Uuid32Model().findById(id).getName());
    }

    @Test
    public void testUuid32InsertBatchShouldGenerateUniqueIds() {
        var a = new Uuid32Model(); a.setName("A"); a.setAge(1);
        var b = new Uuid32Model(); b.setName("B"); b.setAge(2);
        assertTrue(new Uuid32Model().insertBatch(List.of(a, b)));

        assertNotNull(a.getId());
        assertNotNull(b.getId());
        assertNotEquals(a.getId(), b.getId());

        assertNotNull(new Uuid32Model().findById(a.getId()));
        assertNotNull(new Uuid32Model().findById(b.getId()));
    }

    // ==================== UUID36 ====================

    @Test
    public void testUuid36InsertShouldGenerate36CharUuid() {
        var u = new Uuid36Model(); u.setName("U36"); u.setAge(1);
        assertTrue(u.insert());
        String id = u.getId();
        assertNotNull(id);
        assertEquals(36, id.length());
        assertTrue(id.contains("-"));
        assertDoesNotThrow(() -> UUID.fromString(id));
        assertEquals("U36", new Uuid36Model().findById(id).getName());
    }

    @Test
    public void testUuid36InsertBatchShouldGenerateUniqueIds() {
        var a = new Uuid36Model(); a.setName("A"); a.setAge(1);
        var b = new Uuid36Model(); b.setName("B"); b.setAge(2);
        assertTrue(new Uuid36Model().insertBatch(List.of(a, b)));

        assertNotNull(a.getId());
        assertNotNull(b.getId());
        assertNotEquals(a.getId(), b.getId());
        assertDoesNotThrow(() -> UUID.fromString(a.getId()));
        assertDoesNotThrow(() -> UUID.fromString(b.getId()));

        assertNotNull(new Uuid36Model().findById(a.getId()));
        assertNotNull(new Uuid36Model().findById(b.getId()));
    }

    // ==================== AUTO_INCREMENT ====================

    @Test
    public void testAutoIncrementInsertShouldGenerateDbId() {
        var a = new AutoIncrementModel(); a.setName("AI"); a.setAge(1);
        assertTrue(a.insert());
        assertNotNull(a.getId());
        assertTrue(a.getId() > 0);
        assertEquals("AI", new AutoIncrementModel().findById(a.getId()).getName());
    }

    @Test
    public void testAutoIncrementInsertShouldIncreaseSequentially() {
        var a = new AutoIncrementModel(); a.setName("A"); a.setAge(1);
        var b = new AutoIncrementModel(); b.setName("B"); b.setAge(2);
        a.insert(); b.insert();
        assertTrue(a.getId() > 0);
        assertTrue(b.getId() > a.getId());
    }

    @Test
    public void testAutoIncrementInsertBatchShouldBackFillIds() {
        var a = new AutoIncrementModel(); a.setName("A"); a.setAge(1);
        var b = new AutoIncrementModel(); b.setName("B"); b.setAge(2);
        var c = new AutoIncrementModel(); c.setName("C"); c.setAge(3);
        assertTrue(new AutoIncrementModel().insertBatch(List.of(a, b, c)));

        assertNotNull(a.getId());
        assertNotNull(b.getId());
        assertNotNull(c.getId());
        assertTrue(a.getId() < b.getId());
        assertTrue(b.getId() < c.getId());

        assertEquals("A", new AutoIncrementModel().findById(a.getId()).getName());
        assertEquals("B", new AutoIncrementModel().findById(b.getId()).getName());
        assertEquals("C", new AutoIncrementModel().findById(c.getId()).getName());
    }

}
