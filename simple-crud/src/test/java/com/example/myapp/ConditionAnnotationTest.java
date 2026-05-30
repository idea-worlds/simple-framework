package com.example.myapp;

import com.example.myapp.model.UserModel;
import dev.simpleframework.crud.annotation.Condition;
import dev.simpleframework.crud.annotation.Conditions;
import dev.simpleframework.crud.core.ConditionType;
import dev.simpleframework.crud.core.QueryConditions;
import dev.simpleframework.crud.core.QueryConfig;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class ConditionAnnotationTest {

    @Data
    public static class UserQuery {
        @Condition
        private String name;
        @Condition(type = ConditionType.greater_equal)
        private Integer age;
    }

    @Test
    public void testFromAnnotationShouldFilterByConditions() {
        new UserModel() {{ setName("Target"); setAge(30); insert(); }};
        new UserModel() {{ setName("Target"); setAge(10); insert(); }};
        new UserModel() {{ setName("Noise"); setAge(30); insert(); }};

        var query = new UserQuery();
        query.setName("Target");
        query.setAge(30);
        var config = QueryConfig.of().addCondition(QueryConditions.fromAnnotation(query));
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("Target", list.get(0).getName());
        assertEquals(30, list.get(0).getAge());
    }

    @Data
    public static class UserQueryWithDefault {
        @Condition(defaultValueIfNull = "18")
        private Integer age;
    }

    @Test
    public void testFromAnnotationWithDefaultValueIfNull() {
        new UserModel() {{ setName("Adult"); setAge(18); insert(); }};
        new UserModel() {{ setName("Child"); setAge(10); insert(); }};

        var query = new UserQueryWithDefault();
        var config = QueryConfig.of().addCondition(QueryConditions.fromAnnotation(query));
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("Adult", list.get(0).getName());
    }

    @Data
    public static class UserQueryMultiCondition {
        @Condition(field = "age", type = ConditionType.greater_equal)
        private Integer minAge;
        @Condition(field = "age", type = ConditionType.less_equal)
        private Integer maxAge;
    }

    @Test
    public void testFromAnnotationWithMultipleConditionsOnSameField() {
        new UserModel() {{ setName("TooYoung"); setAge(17); insert(); }};
        new UserModel() {{ setName("Adult"); setAge(18); insert(); }};
        new UserModel() {{ setName("Middle"); setAge(30); insert(); }};
        new UserModel() {{ setName("TooOld"); setAge(61); insert(); }};

        var query = new UserQueryMultiCondition();
        query.setMinAge(18);
        query.setMaxAge(60);
        var config = QueryConfig.of().addCondition(QueryConditions.fromAnnotation(query));
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(2, list.size());
    }

    @Data
    public static class UserQueryIn {
        @Condition(field = "age", type = ConditionType.in)
        private List<Integer> ages;
    }

    @Test
    public void testFromAnnotationWithInCondition() {
        new UserModel() {{ setName("A"); setAge(10); insert(); }};
        new UserModel() {{ setName("B"); setAge(20); insert(); }};
        new UserModel() {{ setName("C"); setAge(30); insert(); }};

        var query = new UserQueryIn();
        query.setAges(Arrays.asList(10, 20));
        var config = QueryConfig.of().addCondition(QueryConditions.fromAnnotation(query));
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(2, list.size());
    }

    @Data
    public static class UserQueryLike {
        @Condition(field = "name", type = ConditionType.like_all)
        private String keyword;
    }

    @Test
    public void testFromAnnotationWithLikeAllCondition() {
        new UserModel() {{ setName("HelloWorld"); setAge(1); insert(); }};
        new UserModel() {{ setName("Noise"); setAge(2); insert(); }};

        var query = new UserQueryLike();
        query.setKeyword("loWo");
        var config = QueryConfig.of().addCondition(QueryConditions.fromAnnotation(query));
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("HelloWorld", list.get(0).getName());
    }

    @Data
    public static class UserQueryNull {
        @Condition(field = "email", type = ConditionType.is_null)
        private Boolean dummy;
    }

    @Test
    public void testFromAnnotationWithIsNullCondition() {
        new UserModel() {{ setName("NoEmail"); setAge(1); insert(); }};
        new UserModel() {{ setName("HasEmail"); setAge(2); setEmail("x@x.com"); insert(); }};

        var query = new UserQueryNull();
        var config = QueryConfig.of().addCondition(QueryConditions.fromAnnotation(query));
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("NoEmail", list.get(0).getName());
    }

    @Data
    public static class UserQueryOptional {
        @Condition
        private String name;
        @Condition(type = ConditionType.greater_equal)
        private Integer age;
    }

    @Test
    public void testFromAnnotationWithNullFieldShouldSkipCondition() {
        new UserModel() {{ setName("Any"); setAge(10); insert(); }};
        new UserModel() {{ setName("Any"); setAge(20); insert(); }};

        var query = new UserQueryOptional();
        query.setName("Any");
        var config = QueryConfig.of().addCondition(QueryConditions.fromAnnotation(query));
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(2, list.size());
    }

    @Data
    public static class UserQueryFieldMap {
        @Condition(field = "email")
        private String contact;
    }

    @Test
    public void testFromAnnotationWithFieldMapping() {
        new UserModel() {{ setName("A"); setEmail("a@test.com"); insert(); }};
        new UserModel() {{ setName("B"); setEmail("b@test.com"); insert(); }};

        var query = new UserQueryFieldMap();
        query.setContact("a@test.com");
        var config = QueryConfig.of().addCondition(QueryConditions.fromAnnotation(query));
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("A", list.get(0).getName());
    }

    @Data
    public static class UserQueryDateDefault {
        @Condition(field = "email", defaultValueIfNull = "default@test.com")
        private String email;
    }

    @Test
    public void testFromAnnotationWithStringDefaultValue() {
        new UserModel() {{ setName("Target"); setEmail("default@test.com"); insert(); }};
        new UserModel() {{ setName("Noise"); setEmail("other@test.com"); insert(); }};

        var query = new UserQueryDateDefault();
        var config = QueryConfig.of().addCondition(QueryConditions.fromAnnotation(query));
        List<UserModel> list = new UserModel().listByConditions(config);
        assertEquals(1, list.size());
        assertEquals("Target", list.get(0).getName());
    }

}
