package com.example.myapp;

import com.example.myapp.model.AutoIncrementModel;
import com.example.myapp.model.UuidModel;
import com.example.myapp.model.UserModel;
import dev.simpleframework.crud.annotation.Id;
import dev.simpleframework.crud.core.FieldCustomizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Id 四种主键策略 insert / insertBatch 集成测试。
 * SNOWFLAKE 复用 UserModel，UUID32 使用 UuidModel，
 * UUID36 通过 FieldCustomizer 将 UuidModel.id 切换到 UUID36，
 * AUTO_INCREMENT 使用 AutoIncrementModel。
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Execution(ExecutionMode.SAME_THREAD)
public class IdStrategyIntegrationTest {

    private static final Object LOCK = new Object();

    // ==================== SNOWFLAKE ====================

    @Test
    public void testSnowflakeInsertShouldGenerateLongId() {
        var u = new UserModel(); u.setName("Sf"); u.setAge(1);
        assertTrue(u.insert());
        assertNotNull(u.getId());
        assertTrue(u.getId() > 0, "Snowflake ID should be a positive Long");

        var found = new UserModel().findById(u.getId());
        assertEquals("Sf", found.getName());
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
        var u = new UuidModel(); u.setName("U32"); u.setAge(1);
        assertTrue(u.insert());
        String id = u.getId();
        assertNotNull(id);
        assertEquals(32, id.length(), "UUID32 should be 32 chars");
        assertFalse(id.contains("-"), "UUID32 should have no dashes");

        var found = new UuidModel().findById(id);
        assertEquals("U32", found.getName());
    }

    @Test
    public void testUuid32InsertBatchShouldGenerateUniqueIds() {
        var a = new UuidModel(); a.setName("A"); a.setAge(1);
        var b = new UuidModel(); b.setName("B"); b.setAge(2);
        assertTrue(new UuidModel().insertBatch(List.of(a, b)));

        assertNotNull(a.getId());
        assertNotNull(b.getId());
        assertNotEquals(a.getId(), b.getId(), "UUID32 should be unique per entity");

        assertNotNull(new UuidModel().findById(a.getId()));
        assertNotNull(new UuidModel().findById(b.getId()));
    }

    // ==================== UUID36 (via FieldCustomizer on UuidModel) ====================

    @Test
    public void testUuid36InsertShouldGenerate36CharUuid() {
        synchronized (LOCK) {
            FieldCustomizer.of(UuidModel.class)
                    .field(UuidModel::getId, f -> f.id(Id.Type.UUID36)).apply();
            try {
                var u = new UuidModel(); u.setName("U36"); u.setAge(1);
                assertTrue(u.insert());
                String id = u.getId();
                assertNotNull(id);
                assertEquals(36, id.length(), "UUID36 should be 36 chars");
                assertTrue(id.contains("-"), "UUID36 should have dashes");
                assertDoesNotThrow(() -> UUID.fromString(id));

                var found = new UuidModel().findById(id);
                assertEquals("U36", found.getName());
            } finally {
                FieldCustomizer.of(UuidModel.class)
                        .field(UuidModel::getId, f -> f.id(Id.Type.UUID32)).apply();
            }
        }
    }

    @Test
    public void testUuid36InsertBatchShouldGenerateUniqueIds() {
        synchronized (LOCK) {
            FieldCustomizer.of(UuidModel.class)
                    .field(UuidModel::getId, f -> f.id(Id.Type.UUID36)).apply();
            try {
                var a = new UuidModel(); a.setName("A"); a.setAge(1);
                var b = new UuidModel(); b.setName("B"); b.setAge(2);
                assertTrue(new UuidModel().insertBatch(List.of(a, b)));

                assertNotNull(a.getId());
                assertNotNull(b.getId());
                assertNotEquals(a.getId(), b.getId());
                assertDoesNotThrow(() -> UUID.fromString(a.getId()));
                assertDoesNotThrow(() -> UUID.fromString(b.getId()));

                assertNotNull(new UuidModel().findById(a.getId()));
                assertNotNull(new UuidModel().findById(b.getId()));
            } finally {
                FieldCustomizer.of(UuidModel.class)
                        .field(UuidModel::getId, f -> f.id(Id.Type.UUID32)).apply();
            }
        }
    }

    // ==================== AUTO_INCREMENT ====================

    @Test
    public void testAutoIncrementInsertShouldGenerateDbId() {
        var a = new AutoIncrementModel(); a.setName("AI"); a.setAge(1);
        assertTrue(a.insert());
        assertNotNull(a.getId(), "AUTO_INCREMENT id should be back-filled after insert");
        assertTrue(a.getId() > 0);

        var found = new AutoIncrementModel().findById(a.getId());
        assertEquals("AI", found.getName());
    }

    @Test
    public void testAutoIncrementInsertShouldIncreaseSequentially() {
        var a = new AutoIncrementModel(); a.setName("A"); a.setAge(1);
        var b = new AutoIncrementModel(); b.setName("B"); b.setAge(2);
        a.insert();
        b.insert();
        assertTrue(a.getId() > 0);
        assertTrue(b.getId() > a.getId(), "AUTO_INCREMENT should increase");
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
