package com.example.myapp;

import com.example.myapp.model.UserModel;
import dev.simpleframework.crud.core.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class BaseModelQueryTest {

    // ========== listByConditions ==========

    @Test
    public void testListByConditionsShouldReturnMatchedOnly() {
        new UserModel() {{ setName("Keep"); setAge(1); insert(); }};
        new UserModel() {{ setName("Skip"); setAge(2); insert(); }};
        var config = QueryConfig.of().addCondition("name", "Keep");
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("Keep", list.get(0).getName());
    }

    // ========== QueryFields ==========

    @Test
    public void testQueryFieldsShouldSelectSubset() {
        new UserModel() {{ setName("Fields"); setAge(99); insert(); }};
        var config = QueryConfig.of().addField("name").addCondition("name", "Fields");
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("Fields", list.get(0).getName());
        assertNull(list.get(0).getAge());
    }

    // ========== QuerySorters ==========

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

    // ========== ConditionType — 13 种标准 SQL 条件 ==========

    @Test
    public void testConditionEqual() {
        new UserModel() {{ setName("T"); setAge(1); insert(); }};
        new UserModel() {{ setName("Noise"); setAge(9); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.equal, "T"));
        assertEquals(1, list.size());
        assertEquals("T", list.get(0).getName());
    }

    @Test
    public void testConditionNotEqual() {
        new UserModel() {{ setName("A"); setAge(1); insert(); }};
        new UserModel() {{ setName("B"); setAge(2); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.not_equal, "A")).size());
    }

    @Test
    public void testConditionLikeAll() {
        new UserModel() {{ setName("HelloWorld"); setAge(1); insert(); }};
        new UserModel() {{ setName("Noise"); setAge(9); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.like_all, "loWo"));
        assertEquals(1, list.size());
        assertEquals("HelloWorld", list.get(0).getName());
    }

    @Test
    public void testConditionLikeLeft() {
        new UserModel() {{ setName("HelloWorld"); setAge(1); insert(); }};
        new UserModel() {{ setName("WorldHello"); setAge(9); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.like_left, "World"));
        assertEquals(1, list.size());
        assertEquals("HelloWorld", list.get(0).getName());
    }

    @Test
    public void testConditionLikeRight() {
        new UserModel() {{ setName("HelloWorld"); setAge(1); insert(); }};
        new UserModel() {{ setName("WorldHello"); setAge(9); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("name", ConditionType.like_right, "Hello"));
        assertEquals(1, list.size());
        assertEquals("HelloWorld", list.get(0).getName());
    }

    @Test
    public void testConditionGreaterThan() {
        new UserModel() {{ setName("G"); setAge(50); insert(); }};
        new UserModel() {{ setName("L"); setAge(10); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.greater_than, 30)).size());
    }

    @Test
    public void testConditionGreaterEqual() {
        new UserModel() {{ setName("G"); setAge(50); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.greater_equal, 50)).size());
    }

    @Test
    public void testConditionLessThan() {
        new UserModel() {{ setName("L"); setAge(10); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.less_than, 30)).size());
    }

    @Test
    public void testConditionLessEqual() {
        new UserModel() {{ setName("L"); setAge(10); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.less_equal, 10)).size());
    }

    @Test
    public void testConditionIn() {
        new UserModel() {{ setName("In1"); setAge(1); insert(); }};
        new UserModel() {{ setName("In2"); setAge(2); insert(); }};
        new UserModel() {{ setName("In3"); setAge(3); insert(); }};
        assertEquals(2, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.in, 1, 3)).size());
    }

    @Test
    public void testConditionNotIn() {
        new UserModel() {{ setName("A"); setAge(1); insert(); }};
        new UserModel() {{ setName("B"); setAge(2); insert(); }};
        assertEquals(1, new UserModel().listByConditions(
                QueryConfig.of().addCondition("age", ConditionType.not_in, 2, 99)).size());
    }

    @Test
    public void testConditionIsNull() {
        new UserModel() {{ setName("N"); setAge(1); insert(); }};
        new UserModel() {{ setName("M"); setAge(2); setEmail("x@x.com"); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("email", ConditionType.is_null));
        assertEquals(1, list.size());
        assertEquals("N", list.get(0).getName());
    }

    @Test
    public void testConditionNotNull() {
        new UserModel() {{ setName("N"); setAge(1); insert(); }};
        new UserModel() {{ setName("M"); setAge(2); setEmail("x@x.com"); insert(); }};
        var list = new UserModel().listByConditions(
                QueryConfig.of().addCondition("email", ConditionType.not_null));
        assertEquals(1, list.size());
        assertEquals("M", list.get(0).getName());
    }

    @Test
    public void testConditionNestedAndOr() {
        new UserModel() {{ setName("Young"); setAge(20); insert(); }};
        new UserModel() {{ setName("OldA"); setAge(40); insert(); }};
        new UserModel() {{ setName("OldB"); setAge(50); insert(); }};
        var config = QueryConfig.of()
                .addCondition(QueryConditions.or().add("name", "OldA").add("name", "OldB"))
                .addCondition("age", ConditionType.greater_than, 30);
        assertEquals(2, new UserModel().listByConditions(config).size());
    }

    // ========== pageByConditions ==========

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testPageByConditionsShouldReturnMatchedPage() {
        String prefix = "PageMatch_";
        try {
            for (int i = 0; i < 3; i++) {
                var u = new UserModel(); u.setName(prefix + i); u.setAge(i); u.insert();
            }
            var config = QueryConfig.of()
                    .addCondition("name", ConditionType.like_right, prefix)
                    .addSorter(QuerySorters.asc("age"));
            Page<UserModel> page = new UserModel().pageByConditions(1, 10, config);
            assertEquals(1, page.getPageNum());
            assertEquals(10, page.getPageSize());
            assertEquals(3, page.getTotal());
            assertEquals(1, page.getPages());
            assertEquals(3, page.getItems().size());
            assertEquals(List.of(0, 1, 2), page.getItems().stream().map(UserModel::getAge).toList());
        } finally {
            new UserModel().deleteByConditions(
                    QueryConditions.and().add("name", ConditionType.like_right, prefix));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testPageByConditionsShouldReturnCorrectPage() {
        String prefix = "PageCorrect_";
        try {
            for (int i = 0; i < 15; i++) {
                var u = new UserModel(); u.setName(prefix + i); u.setAge(i); u.insert();
            }
            var page = new UserModel().pageByConditions(1, 5,
                    QueryConfig.of()
                            .addCondition("name", ConditionType.like_right, prefix)
                            .addCondition("age", ConditionType.greater_equal, 0)
                            .addSorter(QuerySorters.asc("age")));
            assertNotNull(page);
            assertEquals(1, page.getPageNum());
            assertEquals(5, page.getPageSize());
            assertEquals(15, page.getTotal());
            assertEquals(3, page.getPages());
            assertEquals(5, page.getItems().size());
            assertEquals(List.of(0, 1, 2, 3, 4), page.getItems().stream().map(UserModel::getAge).toList());
        } finally {
            new UserModel().deleteByConditions(
                    QueryConditions.and().add("name", ConditionType.like_right, prefix));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testPageByConditionsWithSortingShouldReturnSortedPage() {
        String prefix = "PageSorted_";
        try {
            for (int i = 10; i >= 1; i--) {
                var u = new UserModel(); u.setName(prefix + i); u.setAge(i); u.insert();
            }
            Page<UserModel> page = new UserModel().pageByConditions(1, 3,
                    QueryConfig.of()
                            .addCondition("name", ConditionType.like_right, prefix)
                            .addCondition("age", ConditionType.greater_equal, 0)
                            .addSorter(QuerySorters.desc("age")));
            assertEquals(10, page.getTotal());
            assertEquals(3, page.getItems().size());
            assertEquals(List.of(10, 9, 8), page.getItems().stream().map(UserModel::getAge).toList());
        } finally {
            new UserModel().deleteByConditions(
                    QueryConditions.and().add("name", ConditionType.like_right, prefix));
        }
    }

    // ========== 复杂嵌套条件 ==========

    @Test
    public void testThreeLevelNestingShouldFilterCorrectly() {
        new UserModel() {{ setName("T"); setAge(10); insert(); }};
        new UserModel() {{ setName("T"); setAge(20); setEmail("x@x.com"); insert(); }};
        new UserModel() {{ setName("T"); setAge(20); insert(); }};
        new UserModel() {{ setName("X"); setAge(10); insert(); }};

        QueryConditions inner = QueryConditions.and()
                .add("age", ConditionType.equal, 20)
                .add("email", "x@x.com");
        QueryConditions mid = QueryConditions.or()
                .add("age", ConditionType.equal, 10)
                .add(inner);
        var config = QueryConfig.of().addCondition("name", "T").addCondition(mid);
        assertEquals(2, new UserModel().listByConditions(config).size());
    }

    @Test
    public void testOrRootWithAndChildrenShouldMatchEitherGroup() {
        new UserModel() {{ setName("A"); setAge(10); insert(); }};
        new UserModel() {{ setName("B"); setAge(20); insert(); }};
        new UserModel() {{ setName("A"); setAge(20); insert(); }};
        new UserModel() {{ setName("C"); setAge(10); insert(); }};

        QueryConditions and1 = QueryConditions.and().add("name", "A").add("age", 10);
        QueryConditions and2 = QueryConditions.and().add("name", "B").add("age", 20);
        var config = QueryConfig.of().addCondition(QueryConditions.or().add(and1).add(and2));
        assertEquals(2, new UserModel().listByConditions(config).size());
    }

    @Test
    public void testMultipleParallelOrGroupsShouldIntersect() {
        new UserModel() {{ setName("X"); setAge(10); insert(); }};
        new UserModel() {{ setName("X"); setAge(30); insert(); }};
        new UserModel() {{ setName("Z"); setAge(10); insert(); }};
        new UserModel() {{ setName("Y"); setAge(20); insert(); }};

        QueryConditions left = QueryConditions.or().add("name", "X").add("name", "Y");
        QueryConditions right = QueryConditions.or().add("age", 10).add("age", 20);
        var config = QueryConfig.of().addCondition(QueryConditions.and().add(left).add(right));
        assertEquals(2, new UserModel().listByConditions(config).size());
    }

    @Test
    public void testMixedFieldsAndSubConditionsShouldAllApply() {
        new UserModel() {{ setName("Mix"); setAge(10); setEmail("a@test.com"); insert(); }};
        new UserModel() {{ setName("Mix"); setAge(10); setEmail("b@test.com"); insert(); }};
        new UserModel() {{ setName("Mix"); setAge(10); setEmail("c@test.com"); insert(); }};
        new UserModel() {{ setName("Mix"); setAge(3); setEmail("a@test.com"); insert(); }};

        QueryConditions emailOr = QueryConditions.or()
                .add("email", "a@test.com").add("email", "b@test.com");
        var config = QueryConfig.of()
                .addCondition("name", "Mix")
                .addCondition("age", ConditionType.greater_than, 5)
                .addCondition(emailOr);
        assertEquals(2, new UserModel().listByConditions(config).size());
    }

    // ========== 分页 + QueryFields ==========

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testPageByConditionsWithQueryFieldsShouldSelectSubset() {
        String prefix = "PageQF_";
        try {
            for (int i = 0; i < 5; i++) {
                var u = new UserModel(); u.setName(prefix + i); u.setAge(i); u.insert();
            }
            var config = QueryConfig.of()
                    .addField("name")
                    .addCondition("name", ConditionType.like_right, prefix)
                    .addCondition("age", ConditionType.greater_equal, 0)
                    .addSorter(QuerySorters.asc("age"));
            Page<UserModel> page = new UserModel().pageByConditions(1, 10, config);
            assertEquals(5, page.getTotal());
            page.getItems().forEach(u -> {
                assertNotNull(u.getName());
                assertNull(u.getAge());
            });
        } finally {
            new UserModel().deleteByConditions(
                    QueryConditions.and().add("name", ConditionType.like_right, prefix));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testPageByConditionsWithZeroPageSizeShouldBeSafe() {
        String prefix = "PageZero_";
        try {
            for (int i = 0; i < 3; i++) {
                var u = new UserModel(); u.setName(prefix + i); u.setAge(i); u.insert();
            }
            var config = QueryConfig.of()
                    .addCondition("name", ConditionType.like_right, prefix);
            assertDoesNotThrow(() -> new UserModel().pageByConditions(1, 0, config));
        } finally {
            new UserModel().deleteByConditions(
                    QueryConditions.and().add("name", ConditionType.like_right, prefix));
        }
    }

}
