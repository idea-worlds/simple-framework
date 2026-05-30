package dev.simpleframework.crud.test;

import dev.simpleframework.crud.core.QueryFields;
import dev.simpleframework.crud.test.support.TestModelField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueryFieldsTest {

    @Test
    public void testEmpty_shouldReturnAllFields() {
        QueryFields qf = QueryFields.of();
        var fields = Arrays.<dev.simpleframework.crud.ModelField<?>>asList(
                new TestModelField("name", "name", String.class),
                new TestModelField("age", "age", Integer.class)
        );
        assertEquals(2, qf.find(fields).size());
    }

    @Test
    public void testAddSpecific_shouldFilterFields() {
        QueryFields qf = QueryFields.of().add("name");
        var fields = Arrays.<dev.simpleframework.crud.ModelField<?>>asList(
                new TestModelField("name", "name", String.class),
                new TestModelField("age", "age", Integer.class)
        );
        List<?> result = qf.find(fields);
        assertEquals(1, result.size());
        assertEquals("name", ((dev.simpleframework.crud.ModelField<?>) result.get(0)).fieldName());
    }

    @Test
    public void testAddNullString_shouldNotThrow() {
        QueryFields qf = QueryFields.of().add((String[]) null);
        assertNotNull(qf);
    }

    @Test
    public void testAddNullCollection_shouldNotThrow() {
        QueryFields qf = QueryFields.of().add((java.util.Collection<String>) null);
        assertNotNull(qf);
    }

    @Test
    public void testAddEmptyCollection_shouldNotChange() {
        QueryFields qf = QueryFields.of().add("name").add(Collections.emptyList());
        var fields = Arrays.<dev.simpleframework.crud.ModelField<?>>asList(
                new TestModelField("name", "name", String.class)
        );
        assertEquals(1, qf.find(fields).size());
    }

    @Test
    public void testAddLambda_shouldExtractFieldName() {
        QueryFields qf = QueryFields.of().add(com.example.myapp.model.UserModel::getName);
        assertEquals(1, qf.find(List.of(
                new TestModelField("name", "name", String.class),
                new TestModelField("age", "age", Integer.class)
        )).size());
    }

    @Test
    public void testAddQueryFieldsInstance_shouldMerge() {
        QueryFields base = QueryFields.of().add("name");
        QueryFields other = QueryFields.of().add("age");
        base.add(other);
        assertEquals(2, base.find(List.of(
                new TestModelField("name", "name", String.class),
                new TestModelField("age", "age", Integer.class),
                new TestModelField("email", "email", String.class)
        )).size());
    }

    @Test
    public void testCombineFieldsWithNull_shouldReturnEmpty() {
        QueryFields result = QueryFields.combineFields((QueryFields[]) null);
        assertNotNull(result);
    }

    @Test
    public void testCombineFieldsWithMultiple_shouldMergeAll() {
        QueryFields f1 = QueryFields.of().add("name");
        QueryFields f2 = QueryFields.of().add("age");
        QueryFields f3 = QueryFields.of().add("email");
        QueryFields combined = QueryFields.combineFields(f1, f2, f3);
        assertEquals(3, combined.find(List.of(
                new TestModelField("name", "name", String.class),
                new TestModelField("age", "age", Integer.class),
                new TestModelField("email", "email", String.class)
        )).size());
    }

}
