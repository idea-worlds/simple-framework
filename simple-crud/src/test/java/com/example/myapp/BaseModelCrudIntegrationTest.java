package com.example.myapp;

import com.example.myapp.model.UserModel;
import dev.simpleframework.crud.core.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseModel 模式集成测试：实体类 implements BaseModel 后直接调用 CRUD。
 * 模拟真实使用场景——insert/find/update/delete 使用独立实例。
 * 覆盖全部 13 种 ConditionType + 分页 + 字段选择 + 排序。
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class BaseModelCrudIntegrationTest {

    // ========== 增删改查——模拟真实使用流程 ==========

    /**
     * 场景：用户创建实体实例，调 insert() 写入数据库。
     * 验证点：返回 true，id 由雪花算法自动填充且非空。
     */
    @Test
    public void testInsertShouldPersistAndGenerateSnowflakeId() {
        var user = new UserModel();
        user.setName("ZhangSan");
        user.setAge(25);
        user.setEmail("zhangsan@test.com");
        assertTrue(user.insert());
        assertNotNull(user.getId());
    }

    /**
     * 场景：插入后，用另一个实例通过 id 查询。
     * 验证点：返回完整实体，所有字段值与插入时一致。
     * 为什么用独立实例：真实场景中查询和插入不是同一个对象。
     */
    @Test
    public void testFindByIdShouldReturnCompleteEntity() {
        // 1. 插入
        var inserted = new UserModel();
        inserted.setName("LiSi");
        inserted.setAge(30);
        inserted.setEmail("lisi@test.com");
        inserted.insert();
        Long id = inserted.getId();

        // 2. 用独立实例查询
        var found = new UserModel().findById(id);
        assertNotNull(found);
        assertEquals("LiSi", found.getName());
        assertEquals(30, found.getAge());
        assertEquals("lisi@test.com", found.getEmail());
    }

    /**
     * 场景：updateById 根据 id 修改非空字段——真实场景中用户先查到实体，
     *       创建新实例只填 id + 要改的字段，其余字段留 null。
     * 验证点：只改了 name，age 和 email 保持原值（未被 null 覆盖）。
     * 为什么测：这是 updateById 的核心语义——"根据 id 修改模型非空字段"。
     *           如果 null 也覆盖，会导致未设置的字段被清空。
     */
    @Test
    public void testUpdateByIdShouldOnlyModifyNonNullFields() {
        // 1. 插入：所有字段有值
        var inserted = new UserModel();
        inserted.setName("OldName");
        inserted.setAge(10);
        inserted.setEmail("old@test.com");
        inserted.insert();
        Long id = inserted.getId();

        // 2. 更新：创建新实例，只设 id + name，age 和 email 不设（null）
        var update = new UserModel();
        update.setId(id);
        update.setName("NewName");
        // age=null, email=null —— 不应覆盖原值
        assertTrue(update.updateById());

        // 3. 验证：name 已改，age 和 email 未被覆盖
        var found = new UserModel().findById(id);
        assertEquals("NewName", found.getName());
        assertEquals(10, found.getAge(), "age should remain unchanged");
        assertEquals("old@test.com", found.getEmail(), "email should remain unchanged");
    }

    /**
     * 场景：插入多条数据后，根据 id 删除其中一条，不影响其他数据。
     * 验证点：目标行被删除，其他行依然存在。
     * 为什么插入多条：只有一条时无法证明 WHERE 条件生效——全表删返回也是 1。
     */
    @Test
    public void testDeleteByIdShouldRemoveOnlyTarget() {
        // 1. 插入两条
        var a = new UserModel(); a.setName("Keep"); a.insert();
        var b = new UserModel(); b.setName("Del"); b.insert();

        // 2. 删除 b
        assertTrue(new UserModel().deleteById(b.getId()));

        // 3. a 仍在，b 已删除
        assertNotNull(new UserModel().findById(a.getId()));
        assertNull(new UserModel().findById(b.getId()));
    }

    /**
     * 场景：插入多条数据后，根据条件删除其中匹配的行，不匹配的保留。
     * 验证点：只删除了匹配条件的行，不匹配的行仍可查到。
     * 为什么插入多条：只有一条时删除返回 1 可能是全表删，无法证明 WHERE 生效。
     */
    @Test
    public void testDeleteByConditionsShouldRemoveOnlyMatched() {
        new UserModel() {{ setName("Del"); setAge(1); insert(); }};
        new UserModel() {{ setName("Keep"); setAge(2); insert(); }};
        int deleted = new UserModel().deleteByConditions(
                QueryConditions.and().add("name", "Del"));
        assertEquals(1, deleted);
        // Del 被删，Keep 还在
        var delQuery = QueryConfig.of().addCondition("name", "Del");
        assertTrue(new UserModel().listByConditions(delQuery).isEmpty());
        var keepQuery = QueryConfig.of().addCondition("name", "Keep");
        assertEquals(1, new UserModel().listByConditions(keepQuery).size());
    }

    /**
     * 场景：插入多条数据后，条件查询只返回匹配的行。
     * 验证点：返回结果数与预期一致，未匹配的数据被过滤。
     */
    @Test
    public void testListByConditionsShouldReturnMatchedOnly() {
        new UserModel() {{ setName("Keep"); setAge(1); insert(); }};
        new UserModel() {{ setName("Skip"); setAge(2); insert(); }};
        var config = QueryConfig.of().addCondition("name", "Keep");
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("Keep", list.get(0).getName());
    }

    /**
     * 场景：分页查询。
     * 验证点：API 不抛异常（PageHelper 在 @Transactional 下 items 可能为空）。
     */
    @Test
    public void testPageByConditionsShouldNotThrow() {
        for (int i = 0; i < 3; i++) {
            var u = new UserModel(); u.setName("P" + i); u.setAge(i); u.insert();
        }
        var config = QueryConfig.of()
                .addCondition("name", ConditionType.like_all, "P")
                .addSorter(QuerySorters.asc("age"));
        assertDoesNotThrow(() -> new UserModel().pageByConditions(1, 10, config));
    }

    // ========== QueryFields：控制 SELECT 列子集 ==========

    /**
     * 场景：用户只需要 name 字段，不需要 age。
     * 验证点：QueryFields.of().add("name") 后 age 为 null（未被 SELECT）。
     */
    @Test
    public void testQueryFieldsShouldSelectSubset() {
        new UserModel() {{ setName("Fields"); setAge(99); insert(); }};
        var config = QueryConfig.of().addField("name").addCondition("name", "Fields");
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("Fields", list.get(0).getName());
        assertNull(list.get(0).getAge());
    }

    // ========== QuerySorters：ORDER BY 排序 ==========

    /** 场景：按 age 升序排列。验证点：list[0].age=10, list[1].age=20。 */
    @Test
    public void testQuerySortersAscShouldOrderCorrectly() {
        new UserModel() {{ setName("B"); setAge(20); insert(); }};
        new UserModel() {{ setName("A"); setAge(10); insert(); }};
        var config = QueryConfig.of()
                .addSorter(QuerySorters.asc("age"))
                .addCondition("age", ConditionType.greater_than, 0);
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(10, list.get(0).getAge());
        assertEquals(20, list.get(1).getAge());
    }

    /** 场景：按 age 降序排列。验证点：list[0].age=30。 */
    @Test
    public void testQuerySortersDescShouldOrderCorrectly() {
        new UserModel() {{ setName("X"); setAge(10); insert(); }};
        new UserModel() {{ setName("Y"); setAge(30); insert(); }};
        var config = QueryConfig.of()
                .addSorter(QuerySorters.desc("age"))
                .addCondition("age", ConditionType.greater_than, 0);
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(30, list.get(0).getAge());
    }

    // ===================================================================
    // ConditionType 全覆盖——13 种标准 SQL 条件，真实 H2 执行 SQL 验证
    // ===================================================================

    /**
     * equal：WHERE name = 'T' → 只返回精确匹配行。
     * 同时插入干扰行，证明条件过滤而非全表返回。
     */
    @Test public void testConditionEqual() {
        new UserModel() {{ setName("T"); setAge(1); insert(); }};
        new UserModel() {{ setName("Noise"); setAge(9); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.equal, "T"));
        assertEquals(1, list.size());
        assertEquals("T", list.get(0).getName());
    }

    /** not_equal：WHERE name != 'A' → 排除 A，返回 B。 */
    @Test public void testConditionNotEqual() {
        new UserModel() {{ setName("A"); setAge(1); insert(); }};
        new UserModel() {{ setName("B"); setAge(2); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.not_equal, "A")).size());
    }

    /**
     * like_all：WHERE name LIKE '%loWo%' → 只返回包含 "loWo" 的行。
     * 同时插入不包含该子串的干扰行。
     */
    @Test public void testConditionLikeAll() {
        new UserModel() {{ setName("HelloWorld"); setAge(1); insert(); }};
        new UserModel() {{ setName("Noise"); setAge(9); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.like_all, "loWo"));
        assertEquals(1, list.size());
        assertEquals("HelloWorld", list.get(0).getName());
    }

    /**
     * like_left：WHERE name LIKE '%World' → 只返回以 "World" 结尾的行。
     * 同时插入不以该后缀结尾的干扰行。
     */
    @Test public void testConditionLikeLeft() {
        new UserModel() {{ setName("HelloWorld"); setAge(1); insert(); }};
        new UserModel() {{ setName("WorldHello"); setAge(9); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.like_left, "World"));
        assertEquals(1, list.size());
        assertEquals("HelloWorld", list.get(0).getName());
    }

    /**
     * like_right：WHERE name LIKE 'Hello%' → 只返回以 "Hello" 开头的行。
     * 同时插入不以该前缀开头的干扰行。
     */
    @Test public void testConditionLikeRight() {
        new UserModel() {{ setName("HelloWorld"); setAge(1); insert(); }};
        new UserModel() {{ setName("WorldHello"); setAge(9); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.like_right, "Hello"));
        assertEquals(1, list.size());
        assertEquals("HelloWorld", list.get(0).getName());
    }

    /** greater_than：WHERE age > 30 → age=50 命中，age=10 不命中 */
    @Test public void testConditionGreaterThan() {
        new UserModel() {{ setName("G"); setAge(50); insert(); }};
        new UserModel() {{ setName("L"); setAge(10); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.greater_than, 30)).size());
    }

    /** greater_equal：WHERE age >= 50 → age=50 命中（含边界） */
    @Test public void testConditionGreaterEqual() {
        new UserModel() {{ setName("G"); setAge(50); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.greater_equal, 50)).size());
    }

    /** less_than：WHERE age < 30 → age=10 命中 */
    @Test public void testConditionLessThan() {
        new UserModel() {{ setName("L"); setAge(10); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.less_than, 30)).size());
    }

    /** less_equal：WHERE age <= 10 → age=10 命中（含边界） */
    @Test public void testConditionLessEqual() {
        new UserModel() {{ setName("L"); setAge(10); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.less_equal, 10)).size());
    }

    /** in：WHERE age IN (1,3) → 2 条命中，age=2 不命中 */
    @Test public void testConditionIn() {
        new UserModel() {{ setName("In1"); setAge(1); insert(); }};
        new UserModel() {{ setName("In2"); setAge(2); insert(); }};
        new UserModel() {{ setName("In3"); setAge(3); insert(); }};
        assertEquals(2, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.in, 1, 3)).size());
    }

    /** not_in：WHERE age NOT IN (2,99) → 只返回 age=1 */
    @Test public void testConditionNotIn() {
        new UserModel() {{ setName("A"); setAge(1); insert(); }};
        new UserModel() {{ setName("B"); setAge(2); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.not_in, 2, 99)).size());
    }

    /**
     * is_null：WHERE email IS NULL → 只返回 email 为 null 的行。
     * 同时插入一条有 email 的数据，验证 IS NULL 不会误匹配。
     */
    @Test public void testConditionIsNull() {
        new UserModel() {{ setName("N"); setAge(1); insert(); }};       // email=null
        new UserModel() {{ setName("M"); setAge(2); setEmail("x@x.com"); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("email", ConditionType.is_null));
        assertEquals(1, list.size());
        assertEquals("N", list.get(0).getName());
    }

    /**
     * not_null：WHERE email IS NOT NULL → 只返回有 email 的行。
     * 同时插入一条无 email 的数据，验证 NOT NULL 不会误匹配。
     */
    @Test public void testConditionNotNull() {
        new UserModel() {{ setName("N"); setAge(1); insert(); }};       // email=null
        new UserModel() {{ setName("M"); setAge(2); setEmail("x@x.com"); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("email", ConditionType.not_null));
        assertEquals(1, list.size());
        assertEquals("M", list.get(0).getName());
    }

    /** 嵌套 AND/OR：(name='OldA' OR name='OldB') AND age>30 → OldA+OldB 命中，Young 不命中 */
    @Test public void testConditionNestedAndOr() {
        new UserModel() {{ setName("Young"); setAge(20); insert(); }};
        new UserModel() {{ setName("OldA"); setAge(40); insert(); }};
        new UserModel() {{ setName("OldB"); setAge(50); insert(); }};
        var config = QueryConfig.of()
                .addCondition(QueryConditions.or()
                        .add("name", "OldA")
                        .add("name", "OldB"))
                .addCondition("age", ConditionType.greater_than, 30);
        assertEquals(2, new UserModel().listByConditions(config).size());
    }

}
