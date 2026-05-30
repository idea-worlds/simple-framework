package dev.simpleframework.crud.test;

import dev.simpleframework.crud.core.QuerySorters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuerySortersTest {

    @Test
    public void testAsc_shouldMarkAsTrue() {
        QuerySorters sorters = QuerySorters.asc("name");
        assertTrue(sorters.getItems().get("name"));
    }

    @Test
    public void testDesc_shouldMarkAsFalse() {
        QuerySorters sorters = QuerySorters.desc("name");
        assertFalse(sorters.getItems().get("name"));
    }

    @Test
    public void testAddMultipleAsc_shouldPreserveOrder() {
        QuerySorters sorters = QuerySorters.of().addAsc("name").addAsc("age");
        assertEquals(2, sorters.getItems().size());
        assertTrue(sorters.getItems().get("name"));
        assertTrue(sorters.getItems().get("age"));
    }

    @Test
    public void testMixedAscDesc_shouldWork() {
        QuerySorters sorters = QuerySorters.of().addAsc("name").addDesc("age");
        assertTrue(sorters.getItems().get("name"));
        assertFalse(sorters.getItems().get("age"));
    }

    @Test
    public void testAscWithLambda_shouldExtractFieldName() {
        QuerySorters sorters = QuerySorters.asc(com.example.myapp.model.UserModel::getName);
        assertTrue(sorters.getItems().get("name"));
    }

    @Test
    public void testDescWithLambda_shouldExtractFieldName() {
        QuerySorters sorters = QuerySorters.desc(com.example.myapp.model.UserModel::getAge);
        assertFalse(sorters.getItems().get("age"));
    }

}
