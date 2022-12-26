package dev.simpleframework.util.test;

import dev.simpleframework.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class StringsTest {

    @Test
    public void isBlank() {
        Assertions.assertFalse(Strings.isBlank("abc"));
        Assertions.assertFalse(Strings.isBlank("abc "));
        Assertions.assertFalse(Strings.isBlank(" abc"));
        Assertions.assertFalse(Strings.isBlank(" abc "));
        Assertions.assertTrue(Strings.isBlank(" "));
        Assertions.assertTrue(Strings.isBlank("  "));
        Assertions.assertTrue(Strings.isBlank("    "));
    }

    @Test
    public void camelToUnderline() {
        Assertions.assertEquals("strname", Strings.camelToUnderline("strname"));
        Assertions.assertEquals("s_t_r_n_a_m_e", Strings.camelToUnderline("STRNAME"));
        Assertions.assertEquals("str_name", Strings.camelToUnderline("str_name"));
        Assertions.assertEquals("str_name", Strings.camelToUnderline("strName"));
        Assertions.assertEquals("str_name", Strings.camelToUnderline("StrName"));
        Assertions.assertEquals("str_nam_e", Strings.camelToUnderline("StrNamE"));
        Assertions.assertEquals("str_name", Strings.camelToUnderline("Str_Name"));
        Assertions.assertEquals("str_nam_e", Strings.camelToUnderline("Str_Nam_e"));
        Assertions.assertEquals("str_nam__e", Strings.camelToUnderline("Str_Nam__e"));
    }

    @Test
    public void cast() {
        Assertions.assertEquals(1, Strings.cast("1", Integer.class));
        Assertions.assertEquals(2L, Strings.cast("2", Long.class));
        Assertions.assertEquals(1.1, Strings.cast("1.1", Double.class));
        Assertions.assertEquals(new BigDecimal(10), Strings.cast("10", BigDecimal.class));

        Assertions.assertEquals(true, Strings.cast("true", Boolean.class));
        Assertions.assertEquals(true, Strings.cast("true", boolean.class));
        Assertions.assertEquals(false, Strings.cast("false", Boolean.class));
        Assertions.assertEquals(false, Strings.cast("false", boolean.class));
        Assertions.assertEquals(false, Strings.cast("", Boolean.class));
        Assertions.assertEquals(false, Strings.cast("", boolean.class));
        Assertions.assertEquals(false, Strings.cast(null, Boolean.class));
        Assertions.assertEquals(false, Strings.cast(null, boolean.class));

        Assertions.assertEquals(new Date(10000), Strings.cast("10000", Date.class));
        Assertions.assertEquals(new java.sql.Date(10000), Strings.cast("10000", java.sql.Date.class));
        Assertions.assertEquals(new java.sql.Time(10000), Strings.cast("10000", java.sql.Time.class));
        Assertions.assertEquals(new java.sql.Timestamp(10000), Strings.cast("10000", java.sql.Timestamp.class));

        List<?> intList = Strings.cast("[1,1,2]", List.class);
        Assertions.assertEquals(3, intList.size());
        Set<?> intSet = Strings.cast("[1,1,2]", Set.class);
        Assertions.assertEquals(2, intSet.size());
        List<?> strList = Strings.cast("[\"1\",\"1\",\"3\"]", List.class);
        Assertions.assertEquals(3, strList.size());
        Set<?> strSet = Strings.cast("[\"1\",\"1\",\"3\"]", Set.class);
        Assertions.assertEquals(2, strSet.size());

        Assertions.assertEquals(StringsTest.class, Strings.cast(StringsTest.class.getName(), Class.class));
        Assertions.assertThrows(ClassNotFoundException.class, () -> {
            Strings.cast(StringsTest.class.getName() + 1, Class.class);
        });
    }

}
