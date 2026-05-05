package com.example.myapp;

import dev.simpleframework.crud.DynamicModel;
import dev.simpleframework.crud.core.*;
import dev.simpleframework.crud.exception.ModelExecuteException;
import dev.simpleframework.crud.info.dynamic.DynamicModelInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DynamicModel 模式集成测试：运行时动态注册表结构并执行完整 CRUD。
 * 真实场景：用户自定义字段、多租户动态列、ETL 临时表。
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class DynamicModelCrudIntegrationTest {

    /**
     * 场景：运行时构建表结构元信息并注册，注册后 MyBatis SQL 自动生成。
     * 验证点：info() 返回正确的 DynamicModelInfo，getAllFields/InsertFields/UpdateFields/SelectFields 正确。
     */
    @Test
    public void testRegisterShouldStoreModelInfo() {
        var info = new DynamicModelInfo("user_custom", DatasourceType.Mybatis);
        info.addField("name", "name");
        info.addField("age", "age", Integer.class);
        info.addField("id", "id", Long.class);
        info.setId("id");
        DynamicModel.register(info);

        var loaded = DynamicModel.of("user_custom").info();
        assertNotNull(loaded);
        assertEquals("user_custom", loaded.name());
        assertEquals(3, loaded.getAllFields().size());

        DynamicModel.removeRegistered("user_custom");
    }

    /**
     * 场景：动态模型插入数据——设置 data Map 后调 insert()。
     * 验证点：insert() 返回 true，雪花 ID 自动填充到 data Map。
     */
    @Test
    public void testInsertShouldPersistMapData() {
        // 1. 注册动态模型
        var info = new DynamicModelInfo("sys_user", DatasourceType.Mybatis);
        info.addField("name", "name");
        info.addField("age", "age", Integer.class);
        info.addField("id", "id", Long.class);
        info.setId("id");
        DynamicModel.register(info);

        // 2. 创建 DynamicModel 实例，设置数据，插入
        var model = DynamicModel.of("sys_user");
        model.put("name", "DynInsert");
        model.put("age", 28);
        assertTrue(model.insert());
        assertNotNull(model.get("id"));

        DynamicModel.removeRegistered("sys_user");
    }

    /**
     * 场景：插入后通过 findById 查询。
     * 验证点：返回的 Map 中字段值与插入一致。
     */
    @Test
    public void testFindByIdShouldReturnInsertedData() {
        var info = new DynamicModelInfo("sys_user", DatasourceType.Mybatis);
        info.addField("name", "name");
        info.addField("age", "age", Integer.class);
        info.addField("id", "id", Long.class);
        info.setId("id");
        DynamicModel.register(info);

        // 1. 插入两条：目标 + 干扰
        var a = DynamicModel.of("sys_user");
        a.put("name", "Target"); a.put("age", 10); a.insert();
        Long id = (Long) a.get("id");

        var b = DynamicModel.of("sys_user");
        b.put("name", "Noise"); b.put("age", 99); b.insert();

        // 2. findById
        Map<String, Object> result = DynamicModel.of("sys_user").findById(id);
        assertNotNull(result);
        assertEquals("Target", result.get("NAME"));
        assertEquals(10, result.get("AGE"));

        DynamicModel.removeRegistered("sys_user");
    }

    /**
     * 场景：updateById 只修改非空字段。
     * 验证点：修改了 name，age 保持原值。
     */
    @Test
    public void testUpdateByIdShouldOnlyModifyNonNullFields() {
        var info = new DynamicModelInfo("sys_user", DatasourceType.Mybatis);
        info.addField("name", "name");
        info.addField("age", "age", Integer.class);
        info.addField("id", "id", Long.class);
        info.setId("id");
        DynamicModel.register(info);

        // 1. 插入
        var inserted = DynamicModel.of("sys_user");
        inserted.put("name", "OldName");
        inserted.put("age", 10);
        inserted.insert();
        Long id = (Long) inserted.get("id");

        // 2. 更新：新实例只设 name，age 不设
        var update = DynamicModel.of("sys_user");
        update.put("id", id);
        update.put("name", "NewName");
        // age 不设——不应覆盖
        assertTrue(update.updateById());

        // 3. 验证
        Map<String, Object> found = DynamicModel.of("sys_user").findById(id);
        assertEquals("NewName", found.get("NAME"));
        assertEquals(10, found.get("AGE"), "age should remain unchanged");

        DynamicModel.removeRegistered("sys_user");
    }

    /**
     * 场景：listByConditions 条件查询。
     * 验证点：只返回匹配条件的行。
     */
    @Test
    public void testListByConditionsShouldFilter() {
        var info = new DynamicModelInfo("sys_user", DatasourceType.Mybatis);
        info.addField("name", "name");
        info.addField("age", "age", Integer.class);
        info.addField("id", "id", Long.class);
        info.setId("id");
        DynamicModel.register(info);

        var a = DynamicModel.of("sys_user");
        a.put("name", "Keep"); a.put("age", 1); a.insert();

        var b = DynamicModel.of("sys_user");
        b.put("name", "Skip"); b.put("age", 2); b.insert();

        var config = QueryConfig.of().addCondition("name", "Keep");
        List<Map<String, Object>> list = DynamicModel.of("sys_user").listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("Keep", list.get(0).get("NAME"));

        DynamicModel.removeRegistered("sys_user");
    }

    /**
     * 场景：deleteById 删除。
     * 验证点：删除后 findById 返回 null，其他行不受影响。
     */
    @Test
    public void testDeleteByIdShouldRemoveOnlyTarget() {
        var info = new DynamicModelInfo("sys_user", DatasourceType.Mybatis);
        info.addField("name", "name");
        info.addField("id", "id", Long.class);
        info.setId("id");
        DynamicModel.register(info);

        var keep = DynamicModel.of("sys_user");
        keep.put("name", "Keep"); keep.insert();
        Long keepId = (Long) keep.get("id");

        var del = DynamicModel.of("sys_user");
        del.put("name", "Del"); del.insert();
        Long delId = (Long) del.get("id");

        assertTrue(DynamicModel.of("sys_user").deleteById(delId));
        assertNotNull(DynamicModel.of("sys_user").findById(keepId));
        assertNull(DynamicModel.of("sys_user").findById(delId));

        DynamicModel.removeRegistered("sys_user");
    }

    /**
     * 场景：removeRegistered 后 info() 抛异常。
     * 验证点：注销彻底移除了元信息。
     */
    @Test
    public void testRemoveRegisteredShouldCleanUp() {
        var info = new DynamicModelInfo("tmp", DatasourceType.Mybatis);
        info.addField("x", "x");
        info.addField("id", "id", Long.class);
        info.setId("id");
        DynamicModel.register(info);
        assertNotNull(DynamicModel.of("tmp").info());

        DynamicModel.removeRegistered("tmp");
        assertThrows(ModelExecuteException.class, () -> DynamicModel.of("tmp").info());
    }

}
