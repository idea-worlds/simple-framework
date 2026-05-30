package com.example.myapp;

import dev.simpleframework.crud.DynamicModel;
import dev.simpleframework.crud.core.*;
import dev.simpleframework.crud.exception.ModelExecuteException;
import dev.simpleframework.crud.info.dynamic.DynamicModelInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class DynamicModelTest {

    private static final String TABLE = "t_dynamic";

    private static DynamicModelInfo newUserInfo() {
        var info = new DynamicModelInfo(TABLE, DatasourceType.Mybatis);
        info.addField("name", "name");
        info.addField("age", "age", Integer.class);
        info.addField("id", "id", Long.class);
        info.setId("id");
        return info;
    }

    private static void registerAndCleanup(Runnable action) {
        var info = newUserInfo();
        DynamicModel.register(info);
        try {
            action.run();
        } finally {
            DynamicModel.removeRegistered(TABLE);
        }
    }

    @Test
    public void testRegisterShouldStoreModelInfo() {
        registerAndCleanup(() -> {
            var loaded = DynamicModel.of(TABLE).info();
            assertNotNull(loaded);
            assertEquals(TABLE, loaded.name());
            assertEquals(3, loaded.getAllFields().size());
        });
    }

    @Test
    public void testInsertShouldPersistMapData() {
        registerAndCleanup(() -> {
            var model = DynamicModel.of(TABLE);
            model.put("name", "DynInsert");
            model.put("age", 28);
            assertTrue(model.insert());
            assertNotNull(model.get("id"));
        });
    }

    @Test
    public void testFindByIdShouldReturnInsertedData() {
        registerAndCleanup(() -> {
            var a = DynamicModel.of(TABLE);
            a.put("name", "Target"); a.put("age", 10); a.insert();
            Long id = (Long) a.get("id");

            var b = DynamicModel.of(TABLE);
            b.put("name", "Noise"); b.put("age", 99); b.insert();

            Map<String, Object> result = DynamicModel.of(TABLE).findById(id);
            assertNotNull(result);
            assertEquals("Target", result.get("name"));
            assertEquals(10, result.get("age"));
        });
    }

    @Test
    public void testUpdateByIdShouldOnlyModifyNonNullFields() {
        registerAndCleanup(() -> {
            var inserted = DynamicModel.of(TABLE);
            inserted.put("name", "OldName"); inserted.put("age", 10); inserted.insert();
            Long id = (Long) inserted.get("id");

            var update = DynamicModel.of(TABLE);
            update.put("id", id); update.put("name", "NewName");
            assertTrue(update.updateById());

            Map<String, Object> found = DynamicModel.of(TABLE).findById(id);
            assertEquals("NewName", found.get("name"));
            assertEquals(10, found.get("age"));
        });
    }

    @Test
    public void testListByConditionsShouldFilter() {
        registerAndCleanup(() -> {
            var a = DynamicModel.of(TABLE); a.put("name", "Keep"); a.put("age", 1); a.insert();
            var b = DynamicModel.of(TABLE); b.put("name", "Skip"); b.put("age", 2); b.insert();

            var config = QueryConfig.of().addCondition("name", "Keep");
            List<Map<String, Object>> list = DynamicModel.of(TABLE).listByConditions(config);
            assertEquals(1, list.size());
            assertEquals("Keep", list.get(0).get("name"));
        });
    }

    @Test
    public void testDeleteByIdShouldRemoveOnlyTarget() {
        registerAndCleanup(() -> {
            var keep = DynamicModel.of(TABLE); keep.put("name", "Keep"); keep.insert();
            Long keepId = (Long) keep.get("id");
            var del = DynamicModel.of(TABLE); del.put("name", "Del"); del.insert();
            Long delId = (Long) del.get("id");

            assertTrue(DynamicModel.of(TABLE).deleteById(delId));
            assertNotNull(DynamicModel.of(TABLE).findById(keepId));
            assertNull(DynamicModel.of(TABLE).findById(delId));
        });
    }

    @Test
    public void testRemoveRegisteredShouldCleanUp() {
        var info = newUserInfo();
        DynamicModel.register(info);
        try {
            assertNotNull(DynamicModel.of(TABLE).info());
        } finally {
            DynamicModel.removeRegistered(TABLE);
        }
        assertThrows(ModelExecuteException.class, () -> DynamicModel.of(TABLE).info());
    }

    @Test
    public void testCountByConditionsShouldReturnCount() {
        registerAndCleanup(() -> {
            for (int i = 0; i < 5; i++) {
                var m = DynamicModel.of(TABLE); m.put("name", "C" + i); m.put("age", 10 + i); m.insert();
            }
            long count = DynamicModel.of(TABLE).countByConditions(
                    QueryConditions.and().add("age", ConditionType.greater_than, 12));
            assertEquals(2, count);
        });
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testPageByConditionsShouldReturnSortedPage() {
        var info = newUserInfo();
        try {
            DynamicModel.register(info);
            for (int i = 0; i < 5; i++) {
                var m = DynamicModel.of(TABLE); m.put("name", "D" + i); m.put("age", i); m.insert();
            }
            Page<Map<String, Object>> page = DynamicModel.of(TABLE).pageByConditions(1, 3,
                    QueryConfig.of()
                            .addCondition("age", ConditionType.greater_equal, 0)
                            .addSorter(QuerySorters.asc("age")));
            assertEquals(1, page.getPageNum());
            assertEquals(3, page.getPageSize());
            assertEquals(5, page.getTotal());
            assertEquals(2, page.getPages());
            assertEquals(3, page.getItems().size());
            assertEquals(List.of(0, 1, 2), page.getItems().stream().map(item -> item.get("age")).toList());
        } finally {
            DynamicModel.removeRegistered(TABLE);
        }
    }

    // ========== missing CRUD methods ==========

    @Test
    public void testInsertBatchShouldPersistAll() {
        registerAndCleanup(() -> {
            var a = DynamicModel.of(TABLE); a.put("name", "A"); a.put("age", 1);
            var b = DynamicModel.of(TABLE); b.put("name", "B"); b.put("age", 2);
            assertTrue(DynamicModel.of(TABLE).insertBatch(List.of(a, b)));
            assertEquals("A", DynamicModel.of(TABLE).findById(a.get("id")).get("name"));
            assertEquals("B", DynamicModel.of(TABLE).findById(b.get("id")).get("name"));
        });
    }

    @Test
    public void testInsertBatchWithNullShouldReturnFalse() {
        registerAndCleanup(() -> {
            assertFalse(DynamicModel.of(TABLE).insertBatch(null));
        });
    }

    @Test
    public void testDeleteByIdsShouldRemoveTargets() {
        registerAndCleanup(() -> {
            var a = DynamicModel.of(TABLE); a.put("name", "A"); a.insert();
            var b = DynamicModel.of(TABLE); b.put("name", "B"); b.insert();
            var c = DynamicModel.of(TABLE); c.put("name", "C"); c.insert();
            assertTrue(DynamicModel.of(TABLE).deleteByIds(List.of(a.get("id"), c.get("id"))));
            assertNull(DynamicModel.of(TABLE).findById(a.get("id")));
            assertNotNull(DynamicModel.of(TABLE).findById(b.get("id")));
            assertNull(DynamicModel.of(TABLE).findById(c.get("id")));
        });
    }

    @Test
    public void testDeleteByConditionsShouldRemoveMatched() {
        registerAndCleanup(() -> {
            var a = DynamicModel.of(TABLE); a.put("name", "Del"); a.put("age", 1); a.insert();
            var b = DynamicModel.of(TABLE); b.put("name", "Keep"); b.put("age", 2); b.insert();
            int deleted = DynamicModel.of(TABLE).deleteByConditions(QueryConditions.and().add("name", "Del"));
            assertEquals(1, deleted);
            assertTrue(DynamicModel.of(TABLE).listByConditions(QueryConfig.of().addCondition("name", "Del")).isEmpty());
        });
    }

    @Test
    public void testFindOneByConditionsShouldReturnSingle() {
        registerAndCleanup(() -> {
            var a = DynamicModel.of(TABLE); a.put("name", "Match"); a.put("age", 10); a.insert();
            var b = DynamicModel.of(TABLE); b.put("name", "Noise"); b.put("age", 99); b.insert();
            var config = QueryConfig.of().addCondition("name", "Match");
            Map<String, Object> found = DynamicModel.of(TABLE).findOneByConditions(config);
            assertNotNull(found);
            assertEquals("Match", found.get("name"));
            assertEquals(10, found.get("age"));
        });
    }

    @Test
    public void testFindOneByConditionsShouldReturnNullWhenNoMatch() {
        registerAndCleanup(() -> {
            var a = DynamicModel.of(TABLE); a.put("name", "X"); a.put("age", 1); a.insert();
            assertNull(DynamicModel.of(TABLE).findOneByConditions(QueryConfig.of().addCondition("name", "NotExists")));
        });
    }

    @Test
    public void testExistByConditionsShouldReturnTrueAndFalse() {
        registerAndCleanup(() -> {
            var m = DynamicModel.of(TABLE); m.put("name", "Exist"); m.put("age", 1); m.insert();
            assertTrue(DynamicModel.of(TABLE).existByConditions(QueryConditions.and().add("name", "Exist")));
            assertFalse(DynamicModel.of(TABLE).existByConditions(QueryConditions.and().add("name", "Ghost")));
        });
    }

    @Test
    public void testListByIdsShouldReturnOnlyRequested() {
        registerAndCleanup(() -> {
            var a = DynamicModel.of(TABLE); a.put("name", "A"); a.insert();
            var b = DynamicModel.of(TABLE); b.put("name", "B"); b.insert();
            var c = DynamicModel.of(TABLE); c.put("name", "C"); c.insert();
            List<Map<String, Object>> list = DynamicModel.of(TABLE).listByIds(List.of(a.get("id"), b.get("id")));
            assertEquals(2, list.size());
        });
    }

    @Test
    public void testInstanceRegisterShouldWork() {
        try {
            var info = newUserInfo();
            DynamicModel.of(info).register();
            assertNotNull(DynamicModel.of(TABLE).info());
        } finally {
            DynamicModel.removeRegistered(TABLE);
        }
    }

}
