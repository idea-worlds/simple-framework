package com.example.myapp;

import com.example.myapp.model.UserModel;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.core.QueryFields;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class BaseModelCrudTest {

    // ========== insert / findById ==========

    @Test
    public void testInsertShouldGenerateSnowflakeIdAndPersist() {
        var user = new UserModel();
        user.setName("ZhangSan");
        user.setAge(25);
        user.setEmail("zhangsan@test.com");
        assertTrue(user.insert());
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Test
    public void testFindByIdShouldReturnCompleteEntity() {
        var inserted = new UserModel();
        inserted.setName("LiSi");
        inserted.setAge(30);
        inserted.setEmail("lisi@test.com");
        inserted.insert();
        Long id = inserted.getId();

        var found = new UserModel().findById(id);
        assertNotNull(found);
        assertEquals("LiSi", found.getName());
        assertEquals(30, found.getAge());
        assertEquals("lisi@test.com", found.getEmail());
    }

    // ========== updateById ==========

    @Test
    public void testUpdateByIdShouldOnlyModifyNonNullFields() {
        var inserted = new UserModel();
        inserted.setName("OldName");
        inserted.setAge(10);
        inserted.setEmail("old@test.com");
        inserted.insert();
        Long id = inserted.getId();

        var update = new UserModel();
        update.setId(id);
        update.setName("NewName");
        assertTrue(update.updateById());

        var found = new UserModel().findById(id);
        assertEquals("NewName", found.getName());
        assertEquals(10, found.getAge());
        assertEquals("old@test.com", found.getEmail());
    }

    @Test
    public void testUpdateByIdWithOnlyIdShouldThrowException() {
        var u = new UserModel(); u.setName("X"); u.setAge(1); u.insert();
        var update = new UserModel(); update.setId(u.getId());
        assertThrows(Exception.class, update::updateById);
    }

    // ========== deleteById / deleteByIds / deleteByConditions ==========

    @Test
    public void testDeleteByIdShouldRemoveOnlyTarget() {
        var a = new UserModel(); a.setName("Keep"); a.insert();
        var b = new UserModel(); b.setName("Del"); b.insert();

        assertTrue(new UserModel().deleteById(b.getId()));

        assertNotNull(new UserModel().findById(a.getId()));
        assertNull(new UserModel().findById(b.getId()));
    }

    @Test
    public void testDeleteByIdsShouldRemoveOnlyTargets() {
        var a = new UserModel(); a.setName("A"); a.insert();
        var b = new UserModel(); b.setName("B"); b.insert();
        var c = new UserModel(); c.setName("C"); c.insert();

        assertTrue(new UserModel().deleteByIds(List.of(a.getId(), b.getId())));

        assertNull(new UserModel().findById(a.getId()));
        assertNull(new UserModel().findById(b.getId()));
        assertNotNull(new UserModel().findById(c.getId()));
    }

    @Test
    public void testDeleteByConditionsShouldRemoveOnlyMatched() {
        new UserModel() {{ setName("Del"); setAge(1); insert(); }};
        new UserModel() {{ setName("Keep"); setAge(2); insert(); }};
        int deleted = new UserModel().deleteByConditions(QueryConditions.and().add("name", "Del"));
        assertEquals(1, deleted);
        assertTrue(new UserModel().listByConditions(QueryConfig.of().addCondition("name", "Del")).isEmpty());
        assertEquals(1, new UserModel().listByConditions(QueryConfig.of().addCondition("name", "Keep")).size());
    }

    // ========== insertBatch ==========

    @Test
    public void testInsertBatchShouldPersistAll() {
        var a = new UserModel(); a.setName("A"); a.setAge(1);
        var b = new UserModel(); b.setName("B"); b.setAge(2);
        var c = new UserModel(); c.setName("C"); c.setAge(3);
        assertTrue(new UserModel().insertBatch(List.of(a, b, c)));

        var foundA = new UserModel().findById(a.getId());
        assertEquals("A", foundA.getName());
        var foundB = new UserModel().findById(b.getId());
        assertEquals("B", foundB.getName());
        var foundC = new UserModel().findById(c.getId());
        assertEquals("C", foundC.getName());
    }

    // ========== listByIds ==========

    @Test
    public void testListByIdsShouldReturnOnlyRequested() {
        var a = new UserModel(); a.setName("A"); a.insert();
        var b = new UserModel(); b.setName("B"); b.insert();
        var c = new UserModel(); c.setName("C"); c.insert();

        List<UserModel> list = new UserModel().listByIds(List.of(a.getId(), b.getId()));
        assertEquals(2, list.size());
        var ids = list.stream().map(UserModel::getId).toList();
        assertTrue(ids.contains(a.getId()));
        assertTrue(ids.contains(b.getId()));
        assertFalse(ids.contains(c.getId()));
    }

    // ========== count / exist ==========

    @Test
    public void testCountByConditionsShouldReturnCorrectCount() {
        for (int i = 0; i < 3; i++) {
            var u = new UserModel(); u.setName("Big"); u.setAge(30 + i); u.insert();
        }
        for (int i = 0; i < 2; i++) {
            var u = new UserModel(); u.setName("Small"); u.setAge(10 + i); u.insert();
        }
        long count = new UserModel().countByConditions(
                QueryConditions.and().add("age", dev.simpleframework.crud.core.ConditionType.greater_than, 20));
        assertEquals(3, count);
    }

    @Test
    public void testExistByConditionsShouldReturnTrueAndFalse() {
        new UserModel() {{ setName("Exist"); setAge(1); insert(); }};
        assertTrue(new UserModel().existByConditions(QueryConditions.and().add("name", "Exist")));
        assertFalse(new UserModel().existByConditions(QueryConditions.and().add("name", "Ghost")));
    }

    // ========== updateByConditions ==========

    @Test
    public void testUpdateByConditionsShouldModifyOnlyMatched() {
        new UserModel() {{ setName("Old1"); setAge(10); insert(); }};
        new UserModel() {{ setName("Old2"); setAge(15); insert(); }};
        var keep = new UserModel(); keep.setName("Keep"); keep.setAge(30); keep.insert();

        var model = new UserModel();
        model.setName("Updated");
        int updated = model.updateByConditions(
                QueryConditions.and().add("age", dev.simpleframework.crud.core.ConditionType.less_than, 20));
        assertEquals(2, updated);

        var config = QueryConfig.of().addCondition("name", "Updated");
        assertEquals(2, new UserModel().listByConditions(config).size());
        assertEquals("Keep", new UserModel().findById(keep.getId()).getName());
    }

    // ========== findOneByConditions ==========

    @Test
    public void testFindOneByConditionsShouldReturnSingle() {
        new UserModel() {{ setName("Match"); setAge(10); insert(); }};
        new UserModel() {{ setName("Noise"); setAge(99); insert(); }};
        var config = QueryConfig.of().addCondition("name", "Match");
        UserModel found = new UserModel().findOneByConditions(config);
        assertNotNull(found);
        assertEquals("Match", found.getName());
        assertEquals(10, found.getAge());
    }

    @Test
    public void testFindOneByConditionsShouldReturnNullWhenNoMatch() {
        new UserModel() {{ setName("X"); setAge(1); insert(); }};
        var result = new UserModel().findOneByConditions(
                QueryConfig.of().addCondition("name", "NotExists"));
        assertNull(result);
    }

    // ========== 防御性 null / empty 输入 ==========

    @Test
    public void testInsertBatchWithNullShouldReturnFalse() {
        assertFalse(new UserModel().insertBatch(null));
    }

    @Test
    public void testInsertBatchWithEmptyListShouldReturnFalse() {
        assertFalse(new UserModel().insertBatch(List.of()));
    }

    @Test
    public void testDeleteByIdWithNullShouldBeSafe() {
        new UserModel() {{ setName("X"); setAge(1); insert(); }};
        assertDoesNotThrow(() -> new UserModel().deleteById(null));
    }

    @Test
    public void testDeleteByIdsWithNullShouldBeSafe() {
        new UserModel() {{ setName("X"); setAge(1); insert(); }};
        assertDoesNotThrow(() -> new UserModel().deleteByIds(null));
    }

    @Test
    public void testDeleteByIdsWithEmptyListShouldBeSafe() {
        new UserModel() {{ setName("X"); setAge(1); insert(); }};
        assertDoesNotThrow(() -> new UserModel().deleteByIds(List.of()));
    }

    @Test
    public void testDeleteByConditionsWithNullShouldThrow() {
        new UserModel() {{ setName("X"); setAge(1); insert(); }};
        assertThrows(RuntimeException.class, () -> new UserModel().deleteByConditions(null));
    }

    @Test
    public void testDeleteByConditionsWithEmptyConditionsShouldBeSafe() {
        new UserModel() {{ setName("X"); setAge(1); insert(); }};
        assertDoesNotThrow(() -> new UserModel().deleteByConditions(QueryConditions.and()));
    }

    @Test
    public void testUpdateByConditionsWithNullShouldThrow() {
        new UserModel() {{ setName("X"); setAge(1); insert(); }};
        assertThrows(RuntimeException.class, () -> {
            var m = new UserModel(); m.setName("Y");
            m.updateByConditions(null);
        });
    }

    @Test
    public void testFindByIdWithNullShouldReturnNull() {
        assertNull(new UserModel().findById(null));
    }

    @Test
    public void testListByIdsWithNullShouldBeSafe() {
        assertDoesNotThrow(() -> new UserModel().listByIds(null));
    }

    @Test
    public void testListByIdsWithEmptyListShouldReturnEmpty() {
        assertTrue(new UserModel().listByIds(List.of()).isEmpty());
    }

    @Test
    public void testCountByConditionsWithNullShouldThrow() {
        new UserModel() {{ setName("X"); setAge(1); insert(); }};
        assertThrows(RuntimeException.class, () -> new UserModel().countByConditions(null));
    }

    @Test
    public void testExistByConditionsWithNullShouldThrow() {
        assertThrows(RuntimeException.class, () -> new UserModel().existByConditions(null));
    }

    @Test
    public void testFindByIdWithQueryFieldsShouldSelectSubset() {
        var user = new UserModel(); user.setName("QFTest"); user.setAge(99); user.insert();
        var found = new UserModel().findById(user.getId(), QueryFields.of().add("name"));
        assertNotNull(found);
        assertEquals("QFTest", found.getName());
        assertNull(found.getAge());
    }

    @Test
    public void testListByIdsWithQueryFieldsShouldSelectSubset() {
        var a = new UserModel(); a.setName("A"); a.setAge(1); a.insert();
        var b = new UserModel(); b.setName("B"); b.setAge(2); b.insert();
        var list = new UserModel().listByIds(List.of(a.getId(), b.getId()), QueryFields.of().add("name"));
        assertEquals(2, list.size());
        list.forEach(u -> {
            assertNotNull(u.getName());
            assertNull(u.getAge());
        });
    }

}
