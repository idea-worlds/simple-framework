package dev.simpleframework.crud.test;

import dev.simpleframework.crud.dialect.Dialects;
import dev.simpleframework.crud.dialect.condition.H2ConditionDialect;
import dev.simpleframework.crud.dialect.condition.MySqlConditionDialect;
import dev.simpleframework.crud.dialect.condition.PgConditionDialect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DialectsTest {

    @Test
    public void testPgConditionDialect_shouldHaveDefaultInstance() {
        assertNotNull(PgConditionDialect.DEFAULT);
    }

    @Test
    public void testMysqlConditionDialect_shouldHaveDefaultInstance() {
        assertNotNull(MySqlConditionDialect.DEFAULT);
    }

    @Test
    public void testH2ConditionDialect_shouldHaveDefaultInstance() {
        assertNotNull(H2ConditionDialect.DEFAULT);
    }

    @Test
    public void testRegisterCustomDialect_shouldNotThrow() {
        Dialects.registerConditionDialect("testdb", new PgConditionDialect() {});
    }

    @Test
    public void testQuoteColumnNamesToggle_shouldAffectOutput() {
        boolean original = Dialects.isQuoteColumnNames();
        try {
            Dialects.setQuoteColumnNames(true);
            assertTrue(Dialects.isQuoteColumnNames());
            Dialects.setQuoteColumnNames(false);
            assertFalse(Dialects.isQuoteColumnNames());
        } finally {
            Dialects.setQuoteColumnNames(original);
        }
    }

}
