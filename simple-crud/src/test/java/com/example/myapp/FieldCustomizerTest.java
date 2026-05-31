package com.example.myapp;

import com.example.myapp.model.UserModel;
import com.example.operator.model.BaseCascadePojo;
import com.example.operator.model.CascadeChild1Pojo;
import com.example.operator.model.CascadeChild2Pojo;
import com.example.operator.model.UserPojo;
import dev.simpleframework.crud.DynamicModel;
import dev.simpleframework.crud.annotation.DataOperateDate;
import dev.simpleframework.crud.Models;
import dev.simpleframework.crud.core.FieldCustomizer;
import dev.simpleframework.crud.info.dynamic.DynamicModelInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Execution(ExecutionMode.SAME_THREAD)
public class FieldCustomizerTest {

    private static final Object LOCK = new Object();

    // ==================== insertable ====================

    @Test
    public void testBaseModelInsertableFalseShouldSkipField() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserModel.class)
                    .field(UserModel::getEmail, f -> f.insertable(false)).apply();
            try {
                var user = new UserModel();
                user.setName("Test"); user.setAge(1); user.setEmail("ignored@test.com");
                assertTrue(user.insert());
                assertNull(user.findById(user.getId()).getEmail());
            } finally {
                FieldCustomizer.of(UserModel.class)
                        .field(UserModel::getEmail, f -> f.insertable(true)).apply();
            }
        }
    }

    @Test
    public void testOperatorInsertableFalseShouldSkipField() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserPojo.class)
                    .field(UserPojo::getEmail, f -> f.insertable(false)).apply();
            try {
                var pojo = new UserPojo(); pojo.setName("Op"); pojo.setEmail("ignored@test.com");
                Models.wrap(pojo).insert();
                assertNull(Models.wrap(UserPojo.class).findById(pojo.getId()).getEmail());
            } finally {
                FieldCustomizer.of(UserPojo.class)
                        .field(UserPojo::getEmail, f -> f.insertable(true)).apply();
            }
        }
    }

    // ==================== updatable ====================

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
                assertEquals("Original", found.getName());
                assertEquals(99, found.getAge());
            } finally {
                FieldCustomizer.of(UserModel.class)
                        .field(UserModel::getName, f -> f.updatable(true)).apply();
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
                assertEquals("NewName", found.getName());
                assertEquals(10, found.getAge());
            } finally {
                FieldCustomizer.of(UserPojo.class)
                        .field(UserPojo::getAge, f -> f.updatable(true)).apply();
            }
        }
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
                assertNull(found.getName());
                assertEquals(1, found.getAge());
            } finally {
                FieldCustomizer.of(UserModel.class)
                        .field(UserModel::getName, f -> f.selectable(true)).apply();
            }
        }
    }

    @Test
    public void testOperatorSelectableFalseShouldNotReturnField() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserPojo.class)
                    .field(UserPojo::getEmail, f -> f.selectable(false)).apply();
            try {
                var pojo = new UserPojo(); pojo.setName("Op"); pojo.setEmail("hidden@test.com");
                Models.wrap(pojo).insert();
                var found = Models.wrap(UserPojo.class).findById(pojo.getId());
                assertNull(found.getEmail());
            } finally {
                FieldCustomizer.of(UserPojo.class)
                        .field(UserPojo::getEmail, f -> f.selectable(true)).apply();
            }
        }
    }

    // ==================== column name override ====================

    @Test
    public void testBaseModelNameOverrideShouldMapToDifferentColumn() {
        synchronized (LOCK) {
            FieldCustomizer.of(UserModel.class)
                    .field(UserModel::getName, f -> f.name("email")).apply();
            try {
                var user = new UserModel(); user.setName("MappedToEmail"); user.setAge(1); user.insert();
                var found = new UserModel().findById(user.getId());
                assertEquals("MappedToEmail", found.getName());
                assertEquals("MappedToEmail", found.getEmail());
            } finally {
                FieldCustomizer.of(UserModel.class)
                        .field(UserModel::getName, f -> f.name("name")).apply();
            }
        }
    }

    // ==================== cascade ====================

    @Test
    public void testCascadeBaseClassFields() {
        synchronized (LOCK) {
            FieldCustomizer.of(BaseCascadePojo.class)
                    .field(BaseCascadePojo::getCreatedTime, f -> f.autoFill(DataOperateDate.class))
                    .apply();
            var child1 = new CascadeChild1Pojo();
            child1.setName("c1");
            Models.wrap(child1).insert();
            var found1 = Models.wrap(CascadeChild1Pojo.class).findById(child1.getId());
            assertNotNull(found1.getCreatedTime(), "child1 should have auto-filled createdTime");

            var child2 = new CascadeChild2Pojo();
            child2.setName("c2");
            Models.wrap(child2).insert();
            var found2 = Models.wrap(CascadeChild2Pojo.class).findById(child2.getId());
            assertNotNull(found2.getCreatedTime(), "child2 should have auto-filled createdTime");
        }
    }

    @Test
    public void testSubclassOverridesBaseFields() {
        synchronized (LOCK) {
            FieldCustomizer.of(BaseCascadePojo.class)
                    .field(BaseCascadePojo::getCreatedTime, f -> f.autoFill(DataOperateDate.class))
                    .apply();
            FieldCustomizer.of(CascadeChild1Pojo.class)
                    .field(CascadeChild1Pojo::getCreatedTime, f -> f.insertable(false))
                    .apply();
            try {
                var child1 = new CascadeChild1Pojo();
                child1.setName("c1");
                Models.wrap(child1).insert();
                var found1 = Models.wrap(CascadeChild1Pojo.class).findById(child1.getId());
                assertNull(found1.getCreatedTime(), "child1's createdTime should NOT be inserted (overridden)");

                var child2 = new CascadeChild2Pojo();
                child2.setName("c2");
                Models.wrap(child2).insert();
                var found2 = Models.wrap(CascadeChild2Pojo.class).findById(child2.getId());
                assertNotNull(found2.getCreatedTime(), "child2's createdTime should still be auto-filled");
            } finally {
                FieldCustomizer.of(CascadeChild1Pojo.class)
                        .field(CascadeChild1Pojo::getCreatedTime, f -> f.insertable(true))
                        .apply();
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

}
