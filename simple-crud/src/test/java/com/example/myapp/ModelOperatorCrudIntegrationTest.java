package com.example.myapp;

import com.example.operator.model.UserPojo;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.ConditionType;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QueryConfig;
import dev.simpleframework.crud.core.QuerySorters;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelOperator 模式集成测试：零侵入 POJO，不实现 BaseModel 即可使用 CRUD。
 * 模拟真实场景——POJO 来自外部 JAR 或框架约束不允许修改源码，
 * 通过 Models.wrap(entity) / Models.wrap(Class) 接入。
 * 增删改查全部使用独立实例，符合真实业务代码习惯。
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class ModelOperatorCrudIntegrationTest {

    /**
     * 场景：用户创建 POJO，通过 Models.wrap(pojo).insert() 写入。
     * 验证点：返回 true，雪花 ID 自动填充到 pojo.id。
     */
    @Test
    public void testInsertShouldPersistAndGenerateSnowflakeId() {
        var pojo = new UserPojo();
        pojo.setName("OpUser");
        pojo.setAge(20);
        assertTrue(Models.wrap(pojo).insert());
        assertNotNull(pojo.getId());
    }

    /**
     * 场景：插入后用 Models.wrap(Class).findById(id) 查询——不需要实体实例。
     * 验证点：返回完整 POJO，所有字段值与插入一致。
     */
    @Test
    public void testFindByIdShouldReturnCompleteEntity() {
        // 1. 插入
        var inserted = new UserPojo();
        inserted.setName("OpFind");
        inserted.setAge(99);
        inserted.setEmail("opfind@test.com");
        Models.wrap(inserted).insert();
        Long id = inserted.getId();

        // 2. 用独立 API 查询
        var found = Models.wrap(UserPojo.class).findById(id);
        assertNotNull(found);
        assertEquals("OpFind", found.getName());
        assertEquals(99, found.getAge());
        assertEquals("opfind@test.com", found.getEmail());
    }

    /**
     * 场景：updateById 只修改非空字段。真实场景中用户创建新 POJO，
     *       仅设 id + 要改的字段，其余字段为 null 不覆盖。
     * 验证点：name 已改，age 和 email 保持原值。
     */
    @Test
    public void testUpdateByIdShouldOnlyModifyNonNullFields() {
        // 1. 插入：所有字段有值
        var inserted = new UserPojo();
        inserted.setName("OldName");
        inserted.setAge(10);
        inserted.setEmail("old@test.com");
        Models.wrap(inserted).insert();
        Long id = inserted.getId();

        // 2. 更新：新 POJO 只设 id + name，age 和 email 不设（应为 null，不覆盖）
        var update = new UserPojo();
        update.setId(id);
        update.setName("NewName");
        assertTrue(Models.wrap(update).updateById());

        // 3. 验证：name 改了，age 和 email 未变
        var found = Models.wrap(UserPojo.class).findById(id);
        assertEquals("NewName", found.getName());
        assertEquals(10, found.getAge(), "age should remain unchanged");
        assertEquals("old@test.com", found.getEmail(), "email should remain unchanged");
    }

    /**
     * 场景：插入多条后，通过 Models.wrap(Class).deleteById(id) 删除其中一条。
     * 验证点：目标行被删除，其他行不受影响。
     * 为什么插入多条：证明 WHERE id=? 生效而非全表删。
     */
    @Test
    public void testDeleteByIdShouldRemoveOnlyTarget() {
        var a = new UserPojo(); a.setName("Keep"); Models.wrap(a).insert();
        var b = new UserPojo(); b.setName("Del"); Models.wrap(b).insert();

        assertTrue(Models.wrap(UserPojo.class).deleteById(b.getId()));

        assertNotNull(Models.wrap(UserPojo.class).findById(a.getId()));
        assertNull(Models.wrap(UserPojo.class).findById(b.getId()));
    }

    /**
     * 场景：Models.wrap(Class).listByConditions(config) 条件查询。
     * 验证点：只返回匹配条件的记录。
     */
    @Test
    public void testListByConditionsShouldFilterResults() {
        new UserPojo() {{ setName("OpA"); setAge(1); Models.wrap(this).insert(); }};
        new UserPojo() {{ setName("OpB"); setAge(2); Models.wrap(this).insert(); }};
        assertEquals(1, Models.wrap(UserPojo.class).listByConditions(
                QueryConfig.of().addCondition("name", "OpA")).size());
    }

    // ========== updateByConditions / deleteByIds / count / page ==========

    @Test
    public void testUpdateByConditionsShouldModifyMatched() {
        new UserPojo() {{ setName("Old"); setAge(10); Models.wrap(this).insert(); }};
        new UserPojo() {{ setName("Old"); setAge(20); Models.wrap(this).insert(); }};
        var keep = new UserPojo(); keep.setName("Keep"); keep.setAge(10); Models.wrap(keep).insert();

        var update = new UserPojo(); update.setName("New");
        int count = Models.wrap(update).updateByConditions(
                QueryConditions.and().add("name", "Old"));
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
    public void testPageByConditionsShouldNotThrow() {
        for (int i = 0; i < 5; i++) {
            var p = new UserPojo(); p.setName("Pg" + i); p.setAge(i); Models.wrap(p).insert();
        }
        assertDoesNotThrow(() -> Models.wrap(UserPojo.class).pageByConditions(1, 3,
                QueryConfig.of()
                        .addCondition("age", ConditionType.greater_equal, 0)
                        .addSorter(QuerySorters.asc("age"))));
    }

}
