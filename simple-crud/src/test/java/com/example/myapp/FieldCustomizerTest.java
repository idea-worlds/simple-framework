package com.example.myapp;

import com.example.myapp.model.DateModel;
import com.example.myapp.model.UserModel;
import com.example.operator.model.UserPojo;
import dev.simpleframework.crud.DynamicModel;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.FieldCustomizer;
import dev.simpleframework.crud.info.dynamic.DynamicModelInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldCustomizer 字段策略覆盖集成测试。
 *
 * 注意：FieldCustomizer.apply() 修改 ModelCache 全局状态。
 * 每次操作用 synchronized 锁保护，try-finally 确保恢复原始配置。
 * @Execution(SAME_THREAD) 防止并发执行下的竞态。
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Execution(ExecutionMode.SAME_THREAD)
public class FieldCustomizerTest {

    private static final Object LOCK = new Object();

    // ==================== BaseModel ====================

    @Test
    public void testBaseModelInsertableFalseShouldSkipField() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserModel.class)
                    .field(UserModel::getEmail, f -> f.insertable(false)).apply();
            try {
                var user = new UserModel();
                user.setName("Test"); user.setAge(1); user.setEmail("ignored@test.com");
                assertTrue(user.insert());
                assertNull(user.findById(user.getId()).getEmail(),
                        "email should be null because insertable=false");
            } finally {
                FieldCustomizer.of(UserModel.class)
                        .field(UserModel::getEmail, f -> f.insertable(true)).apply();
            }
        }
    }

    @Test
    public void testBaseModelUpdatableFalseShouldRetainOriginal() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserModel.class)
                    .field(UserModel::getName, f -> f.updatable(false)).apply();
            try {
                var inserted = new UserModel(); inserted.setName("Original"); inserted.setAge(10); inserted.insert();
                Long id = inserted.getId();
                var update = new UserModel(); update.setId(id); update.setName("New"); update.setAge(99);
                assertTrue(update.updateById());
                var found = new UserModel().findById(id);
                assertEquals("Original", found.getName(), "name should remain unchanged");
                assertEquals(99, found.getAge(), "age should be updated");
            } finally {
                FieldCustomizer.of(UserModel.class)
                        .field(UserModel::getName, f -> f.updatable(true)).apply();
            }
        }
    }

    // ==================== ModelOperator ====================

    @Test
    public void testOperatorInsertableFalseShouldSkipField() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserPojo.class)
                    .field(UserPojo::getEmail, f -> f.insertable(false)).apply();
            try {
                var pojo = new UserPojo(); pojo.setName("Op"); pojo.setEmail("ignored@test.com");
                Models.wrap(pojo).insert();
                assertNull(Models.wrap(UserPojo.class).findById(pojo.getId()).getEmail(),
                        "email should be null because insertable=false");
            } finally {
                FieldCustomizer.of(UserPojo.class)
                        .field(UserPojo::getEmail, f -> f.insertable(true)).apply();
            }
        }
    }

    @Test
    public void testOperatorUpdatableFalseShouldRetainOriginal() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserPojo.class)
                    .field(UserPojo::getAge, f -> f.updatable(false)).apply();
            try {
                var inserted = new UserPojo(); inserted.setName("Op"); inserted.setAge(10);
                Models.wrap(inserted).insert(); Long id = inserted.getId();
                var update = new UserPojo(); update.setId(id); update.setName("NewName"); update.setAge(99);
                assertTrue(Models.wrap(update).updateById());
                var found = Models.wrap(UserPojo.class).findById(id);
                assertEquals("NewName", found.getName(), "name should be updated");
                assertEquals(10, found.getAge(), "age should remain unchanged");
            } finally {
                FieldCustomizer.of(UserPojo.class)
                        .field(UserPojo::getAge, f -> f.updatable(true)).apply();
            }
        }
    }

    // ==================== DynamicModel ====================

    @Test
    public void testDynamicFieldCustomizerViaInfoApi() {
        var info = new DynamicModelInfo("dyn_fc", dev.simpleframework.crud.core.DatasourceType.Mybatis);
        info.addField("name", "name");
        info.addField("id", "id", Long.class);
        info.setId("id");
        DynamicModel.register(info);
        assertNotNull(DynamicModel.of("dyn_fc").info());
        DynamicModel.removeRegistered("dyn_fc");
    }

    // ==================== selectable ====================

    @Test
    public void testBaseModelSelectableFalseShouldNotReturnField() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserModel.class)
                    .field(UserModel::getName, f -> f.selectable(false)).apply();
            try {
                var user = new UserModel(); user.setName("Hidden"); user.setAge(1); user.insert();
                var found = new UserModel().findById(user.getId());
                assertNull(found.getName(), "name should be null because selectable=false");
                assertEquals(1, found.getAge(), "age should still be returned");
            } finally {
                FieldCustomizer.of(UserModel.class)
                        .field(UserModel::getName, f -> f.selectable(true)).apply();
            }
        }
    }

    // ==================== column name override ====================

    @Test
    public void testBaseModelNameOverrideShouldMapToDifferentColumn() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserModel.class)
                    .field(UserModel::getName, f -> f.name("name2")).apply();
            try {
                var user = new UserModel(); user.setName("Mapped"); user.setAge(1); user.insert();
                var found = new UserModel().findById(user.getId());
                assertEquals("Mapped", found.getName(),
                        "name field is mapped to name2 column");
            } finally {
                FieldCustomizer.of(UserModel.class)
                        .field(UserModel::getName, f -> f.name("name")).apply();
            }
        }
    }

    // ==================== ModelOperator selectable ====================

    @Test
    public void testOperatorSelectableFalseShouldNotReturnField() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserPojo.class)
                    .field(UserPojo::getEmail, f -> f.selectable(false)).apply();
            try {
                var pojo = new UserPojo(); pojo.setName("Op"); pojo.setEmail("hidden@test.com");
                Models.wrap(pojo).insert();
                var found = Models.wrap(UserPojo.class).findById(pojo.getId());
                assertNull(found.getEmail(),
                        "email should be null because selectable=false");
            } finally {
                FieldCustomizer.of(UserPojo.class)
                        .field(UserPojo::getEmail, f -> f.selectable(true)).apply();
            }
        }
    }

}
