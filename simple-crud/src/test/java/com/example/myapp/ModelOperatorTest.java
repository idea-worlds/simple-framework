package com.example.myapp;

import com.example.operator.model.UserPojo;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.ConditionType;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.core.QuerySorters;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class ModelOperatorTest {

    // ========== defensive null input ==========

    @Test
    public void testWrapWithNullEntityShouldThrowNpe() {
        assertThrows(NullPointerException.class, () -> Models.wrap(null));
    }

    @Test
    public void testWrapWithNullClassShouldThrowNpe() {
        assertThrows(NullPointerException.class, () -> Models.wrap((Class<?>) null));
    }

    @Test
    public void testClassBoundInsertShouldThrow() {
        assertThrows(dev.simpleframework.crud.exception.ModelExecuteException.class,
                () -> Models.wrap(UserPojo.class).insert());
    }

    @Test
    public void testClassBoundUpdateByIdShouldThrow() {
        assertThrows(dev.simpleframework.crud.exception.ModelExecuteException.class,
                () -> Models.wrap(UserPojo.class).updateById());
    }

    // ========== normal CRUD ==========

    @Test
    public void testInsertShouldPersistAndGenerateSnowflakeId() {
        var pojo = new UserPojo();
        pojo.setName("OpUser");
        pojo.setAge(20);
        assertTrue(Models.wrap(pojo).insert());
        assertNotNull(pojo.getId());
    }

    @Test
    public void testFindByIdShouldReturnCompleteEntity() {
        var inserted = new UserPojo();
        inserted.setName("OpFind");
        inserted.setAge(99);
        inserted.setEmail("opfind@test.com");
        Models.wrap(inserted).insert();
        Long id = inserted.getId();

        var found = Models.wrap(UserPojo.class).findById(id);
        assertNotNull(found);
        assertEquals("OpFind", found.getName());
        assertEquals(99, found.getAge());
        assertEquals("opfind@test.com", found.getEmail());
    }

    @Test
    public void testUpdateByIdShouldOnlyModifyNonNullFields() {
        var inserted = new UserPojo();
        inserted.setName("OldName");
        inserted.setAge(10);
        inserted.setEmail("old@test.com");
        Models.wrap(inserted).insert();
        Long id = inserted.getId();

        var update = new UserPojo();
        update.setId(id);
        update.setName("NewName");
        assertTrue(Models.wrap(update).updateById());

        var found = Models.wrap(UserPojo.class).findById(id);
        assertEquals("NewName", found.getName());
        assertEquals(10, found.getAge());
        assertEquals("old@test.com", found.getEmail());
    }

    @Test
    public void testDeleteByIdShouldRemoveOnlyTarget() {
        var a = new UserPojo(); a.setName("Keep"); Models.wrap(a).insert();
        var b = new UserPojo(); b.setName("Del"); Models.wrap(b).insert();

        assertTrue(Models.wrap(UserPojo.class).deleteById(b.getId()));
        assertNotNull(Models.wrap(UserPojo.class).findById(a.getId()));
        assertNull(Models.wrap(UserPojo.class).findById(b.getId()));
    }

    @Test
    public void testListByConditionsShouldFilterResults() {
        new UserPojo() {{ setName("OpA"); setAge(1); Models.wrap(this).insert(); }};
        new UserPojo() {{ setName("OpB"); setAge(2); Models.wrap(this).insert(); }};
        assertEquals(1, Models.wrap(UserPojo.class).listByConditions(
                QueryConfig.of().addCondition("name", "OpA")).size());
    }

    @Test
    public void testUpdateByConditionsShouldModifyMatched() {
        new UserPojo() {{ setName("Old"); setAge(10); Models.wrap(this).insert(); }};
        new UserPojo() {{ setName("Old"); setAge(20); Models.wrap(this).insert(); }};
        var keep = new UserPojo(); keep.setName("Keep"); keep.setAge(10); Models.wrap(keep).insert();

        var update = new UserPojo(); update.setName("New");
        int count = Models.wrap(update).updateByConditions(QueryConditions.and().add("name", "Old"));
        assertEquals(2, count);
        assertEquals(2, Models.wrap(UserPojo.class).listByConditions(
                QueryConfig.of().addCondition("name", "New")).size());
        assertEquals("Keep", Models.wrap(UserPojo.class).findById(keep.getId()).getName());
    }

    @Test
    public void testDeleteByIdsShouldRemoveTargets() {
        var a = new UserPojo(); a.setName("A"); Models.wrap(a).insert();
        var b = new UserPojo(); b.setName("B"); Models.wrap(b).insert();
        var c = new UserPojo(); c.setName("C"); Models.wrap(c).insert();

        assertTrue(Models.wrap(UserPojo.class).deleteByIds(List.of(a.getId(), c.getId())));
        assertNull(Models.wrap(UserPojo.class).findById(a.getId()));
        assertNotNull(Models.wrap(UserPojo.class).findById(b.getId()));
        assertNull(Models.wrap(UserPojo.class).findById(c.getId()));
    }

    @Test
    public void testCountByConditionsShouldReturnCount() {
        for (int i = 0; i < 3; i++) {
            var p = new UserPojo(); p.setName("Cnt"); p.setAge(20 + i); Models.wrap(p).insert();
        }
        long count = Models.wrap(UserPojo.class).countByConditions(
                QueryConditions.and().add("age", ConditionType.greater_equal, 21));
        assertEquals(2, count);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testPageByConditionsShouldReturnSortedPage() {
        String prefix = "OpPage_";
        try {
            for (int i = 0; i < 5; i++) {
                var p = new UserPojo(); p.setName(prefix + i); p.setAge(i); Models.wrap(p).insert();
            }
            var page = Models.wrap(UserPojo.class).pageByConditions(1, 3,
                    QueryConfig.of()
                            .addCondition("name", ConditionType.like_right, prefix)
                            .addCondition("age", ConditionType.greater_equal, 0)
                            .addSorter(QuerySorters.asc("age")));
            assertEquals(1, page.getPageNum());
            assertEquals(3, page.getPageSize());
            assertEquals(5, page.getTotal());
            assertEquals(2, page.getPages());
            assertEquals(3, page.getItems().size());
            assertEquals(List.of(0, 1, 2), page.getItems().stream().map(UserPojo::getAge).toList());
        } finally {
            Models.wrap(UserPojo.class).deleteByConditions(
                    QueryConditions.and().add("name", ConditionType.like_right, prefix));
        }
    }

}
