package dev.simpleframework.dag;

import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.FilterAction;
import dev.simpleframework.dag.engine.pipeline.sink.filter.ExpressionFilter;
import dev.simpleframework.dag.engine.pipeline.sink.filter.SizeGreaterThan;
import dev.simpleframework.dag.engine.pipeline.sink.filter.SizeLessEqual;
import dev.simpleframework.dag.engine.pipeline.sink.filter.SizeLessThan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author loyayz
 **/
public class FilterActionTest {

    @Test
    public void always() {
        FilterAction action = FilterAction.always;
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertTrue(action.filter("a", null));
        Assertions.assertTrue(action.filter("a", map));
        Assertions.assertTrue(action.filter("a", list));
        Assertions.assertTrue(action.filter("a", array));
    }

    @Test
    public void is_equals() {
        FilterAction action = FilterAction.is_equals;
        Assertions.assertTrue(action.filter("a", "a"));
        Assertions.assertTrue(action.filter(1, 1));
        Assertions.assertTrue(action.filter(2L, 2L));
        for (int i = 0; i < 10; i++) {
            long num = ThreadLocalRandom.current().nextLong();
            Assertions.assertTrue(action.filter(new BigDecimal(num), new BigDecimal(num)));
        }
        Date date = new Date();
        Assertions.assertTrue(action.filter(date, new Date(date.getTime())));
    }

    @Test
    public void not_equals() {
        FilterAction action = FilterAction.not_equals;
        Assertions.assertFalse(action.filter("a", "a"));
        Assertions.assertTrue(action.filter("a", "b"));
        Assertions.assertFalse(action.filter(1, 1));
        Assertions.assertTrue(action.filter(2L, 3L));
        for (int i = 0; i < 10; i++) {
            long num = ThreadLocalRandom.current().nextLong();
            Assertions.assertFalse(action.filter(new BigDecimal(num), new BigDecimal(num)));
            Assertions.assertTrue(action.filter(new BigDecimal(num), new BigDecimal(num + 1)));
        }
        Date date = new Date();
        Assertions.assertFalse(action.filter(date, new Date(date.getTime())));
        Assertions.assertTrue(action.filter(date, new Date(date.getTime() + 1000)));
    }

    @Test
    public void greater_than() {
        FilterAction action = FilterAction.greater_than;
        Assertions.assertFalse(action.filter("a", "a"));
        Assertions.assertTrue(action.filter("b", "a"));
        Assertions.assertFalse(action.filter("a", "b"));
        Assertions.assertFalse(action.filter(1, 1));
        Assertions.assertTrue(action.filter(1, 0));
        Assertions.assertFalse(action.filter(2L, 2L));
        Assertions.assertTrue(action.filter(2L, 1L));
        for (int i = 0; i < 10; i++) {
            long num = ThreadLocalRandom.current().nextLong();
            Assertions.assertFalse(action.filter(new BigDecimal(num), new BigDecimal(num)));
            Assertions.assertTrue(action.filter(new BigDecimal(num), new BigDecimal(num - 1)));
        }
        Date date = new Date();
        Assertions.assertFalse(action.filter(date, new Date(date.getTime())));
        Assertions.assertTrue(action.filter(date, new Date(date.getTime() - 1000)));
    }

    @Test
    public void great_equal() {
        FilterAction action = FilterAction.great_equal;
        Assertions.assertTrue(action.filter("a", "a"));
        Assertions.assertTrue(action.filter("b", "a"));
        Assertions.assertFalse(action.filter("a", "b"));
        Assertions.assertTrue(action.filter(1, 1));
        Assertions.assertTrue(action.filter(1, 0));
        Assertions.assertTrue(action.filter(2L, 2L));
        Assertions.assertTrue(action.filter(2L, 1L));
        for (int i = 0; i < 10; i++) {
            long num = ThreadLocalRandom.current().nextLong();
            Assertions.assertTrue(action.filter(new BigDecimal(num), new BigDecimal(num)));
            Assertions.assertTrue(action.filter(new BigDecimal(num), new BigDecimal(num - 1)));
        }
        Date date = new Date();
        Assertions.assertTrue(action.filter(date, new Date(date.getTime())));
        Assertions.assertTrue(action.filter(date, new Date(date.getTime() - 1000)));
    }

    @Test
    public void less_than() {
        FilterAction action = FilterAction.less_than;
        Assertions.assertFalse(action.filter("a", "a"));
        Assertions.assertFalse(action.filter("b", "a"));
        Assertions.assertTrue(action.filter("a", "b"));
        Assertions.assertFalse(action.filter(1, 1));
        Assertions.assertTrue(action.filter(1, 2));
        Assertions.assertFalse(action.filter(2L, 2L));
        Assertions.assertTrue(action.filter(2L, 3L));
        for (int i = 0; i < 10; i++) {
            long num = ThreadLocalRandom.current().nextLong();
            Assertions.assertFalse(action.filter(new BigDecimal(num), new BigDecimal(num)));
            Assertions.assertTrue(action.filter(new BigDecimal(num), new BigDecimal(num + 1)));
        }
        Date date = new Date();
        Assertions.assertFalse(action.filter(date, new Date(date.getTime())));
        Assertions.assertTrue(action.filter(date, new Date(date.getTime() + 1000)));
    }

    @Test
    public void less_equal() {
        FilterAction action = FilterAction.less_equal;
        Assertions.assertTrue(action.filter("a", "a"));
        Assertions.assertFalse(action.filter("b", "a"));
        Assertions.assertTrue(action.filter("a", "b"));
        Assertions.assertTrue(action.filter(1, 1));
        Assertions.assertTrue(action.filter(1, 2));
        Assertions.assertTrue(action.filter(2L, 2L));
        Assertions.assertTrue(action.filter(2L, 3L));
        for (int i = 0; i < 10; i++) {
            long num = ThreadLocalRandom.current().nextLong();
            Assertions.assertTrue(action.filter(new BigDecimal(num), new BigDecimal(num)));
            Assertions.assertTrue(action.filter(new BigDecimal(num), new BigDecimal(num + 1)));
        }
        Date date = new Date();
        Assertions.assertTrue(action.filter(date, new Date(date.getTime())));
        Assertions.assertTrue(action.filter(date, new Date(date.getTime() + 1000)));
    }

    @Test
    public void is_null() {
        FilterAction action = FilterAction.is_null;
        Assertions.assertTrue(action.filter(null, null));
        Assertions.assertFalse(action.filter("a", null));
        Assertions.assertFalse(action.filter(1, null));
    }

    @Test
    public void not_null() {
        FilterAction action = FilterAction.not_null;
        Assertions.assertFalse(action.filter(null, null));
        Assertions.assertTrue(action.filter("a", null));
        Assertions.assertTrue(action.filter(1, null));
    }

    @Test
    public void is_empty() {
        FilterAction action = FilterAction.is_empty;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertTrue(action.filter("", null));
        Assertions.assertFalse(action.filter(str, null));
        Assertions.assertFalse(action.filter(map, null));
        Assertions.assertFalse(action.filter(list, null));
        Assertions.assertFalse(action.filter(array, null));
    }

    @Test
    public void not_empty() {
        FilterAction action = FilterAction.not_empty;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertFalse(action.filter("", null));
        Assertions.assertTrue(action.filter(str, null));
        Assertions.assertTrue(action.filter(map, null));
        Assertions.assertTrue(action.filter(list, null));
        Assertions.assertTrue(action.filter(array, null));
    }

    @Test
    public void is_contains_all() {
        FilterAction action = FilterAction.is_contains_all;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertTrue(action.filter(str, List.of("a", "ab", "abc", "b", "bc", "c")));
        Assertions.assertFalse(action.filter(str, List.of("a", "ab", "abc", "b", "bc", "c", "d")));
        Assertions.assertFalse(action.filter(str, "abcd"));
        Assertions.assertFalse(action.filter(str, "d"));
        Assertions.assertTrue(action.filter(map, List.of("a", "b")));
        Assertions.assertFalse(action.filter(map, List.of("c", "d", "e")));
        Assertions.assertTrue(action.filter(list, List.of("1", "22", "333", "4444")));
        Assertions.assertFalse(action.filter(list, List.of("1", "22", "333", "4444", "5")));
        Assertions.assertTrue(action.filter(array, 1));
        Assertions.assertTrue(action.filter(array, List.of(1, 2, 3)));
        Assertions.assertFalse(action.filter(array, 11));
        Assertions.assertTrue(action.filter(map, Collections.emptyList()));
        Assertions.assertTrue(action.filter(list, Collections.emptyList()));
        Assertions.assertTrue(action.filter(array, Collections.emptyList()));
        Assertions.assertFalse(action.filter(str, null));
        Assertions.assertFalse(action.filter(map, null));
        Assertions.assertFalse(action.filter(list, null));
        Assertions.assertFalse(action.filter(array, null));
    }

    @Test
    public void is_contains_any() {
        FilterAction action = FilterAction.is_contains_any;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertTrue(action.filter(str, List.of("a", "ab", "abc", "b", "bc", "c")));
        Assertions.assertTrue(action.filter(str, List.of("a", "ab", "abc", "b", "bc", "c", "d")));
        Assertions.assertFalse(action.filter(str, "abcd"));
        Assertions.assertFalse(action.filter(str, "d"));
        Assertions.assertTrue(action.filter(map, List.of("a", "b")));
        Assertions.assertTrue(action.filter(map, List.of("c", "d", "e")));
        Assertions.assertTrue(action.filter(list, List.of("1", "22", "333", "4444")));
        Assertions.assertTrue(action.filter(list, List.of("1", "22", "333", "4444", "5")));
        Assertions.assertTrue(action.filter(array, 1));
        Assertions.assertTrue(action.filter(array, List.of(1, 11, 111)));
        Assertions.assertFalse(action.filter(array, List.of(11, 111)));
        Assertions.assertFalse(action.filter(array, 11));
        Assertions.assertTrue(action.filter(map, Collections.emptyList()));
        Assertions.assertTrue(action.filter(list, Collections.emptyList()));
        Assertions.assertTrue(action.filter(array, Collections.emptyList()));
        Assertions.assertFalse(action.filter(str, null));
        Assertions.assertFalse(action.filter(map, null));
        Assertions.assertFalse(action.filter(list, null));
        Assertions.assertFalse(action.filter(array, null));
    }

    @Test
    public void is_contained_by() {
        FilterAction action = FilterAction.is_contained_by;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertTrue(action.filter(str, "abc"));
        Assertions.assertTrue(action.filter(str, List.of("abc", "bcd")));
        Assertions.assertTrue(action.filter(str, "abcd"));
        Assertions.assertTrue(action.filter(str, List.of("abcd", "bcd")));
        Assertions.assertFalse(action.filter(str, "bcd"));
        Assertions.assertFalse(action.filter(map, List.of("a", "b")));
        Assertions.assertTrue(action.filter(map, List.of("a", "b", "c")));
        Assertions.assertTrue(action.filter(map, List.of("a", "b", "c", "d")));
        Assertions.assertFalse(action.filter(list, List.of("1", "22", "333")));
        Assertions.assertTrue(action.filter(list, List.of("1", "22", "333", "4444")));
        Assertions.assertTrue(action.filter(list, List.of("1", "22", "333", "4444", "5")));
        Assertions.assertFalse(action.filter(array, 1));
        Assertions.assertFalse(action.filter(str, null));
        Assertions.assertFalse(action.filter(map, null));
        Assertions.assertFalse(action.filter(list, null));
        Assertions.assertFalse(action.filter(array, null));
    }

    @Test
    public void not_contains() {
        FilterAction action = FilterAction.not_contains;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertFalse(action.filter(str, List.of("a", "ab", "abc", "b", "bc", "c")));
        Assertions.assertFalse(action.filter(str, List.of("a", "ab", "abc", "b", "bc", "c", "d")));
        Assertions.assertTrue(action.filter(str, "abcd"));
        Assertions.assertTrue(action.filter(str, "d"));
        Assertions.assertFalse(action.filter(map, List.of("a", "b")));
        Assertions.assertFalse(action.filter(map, List.of("a", "b", "c")));
        Assertions.assertTrue(action.filter(map, List.of("a", "b", "c", "d")));
        Assertions.assertFalse(action.filter(list, List.of("1", "22", "333")));
        Assertions.assertFalse(action.filter(list, List.of("1", "22", "333", "4444")));
        Assertions.assertTrue(action.filter(list, List.of("1", "22", "333", "4444", "5")));
        Assertions.assertFalse(action.filter(array, 1));
        Assertions.assertTrue(action.filter(array, 11));
        Assertions.assertTrue(action.filter(map, Collections.emptyList()));
        Assertions.assertTrue(action.filter(list, Collections.emptyList()));
        Assertions.assertTrue(action.filter(array, Collections.emptyList()));
        Assertions.assertFalse(action.filter(str, null));
        Assertions.assertFalse(action.filter(map, null));
        Assertions.assertFalse(action.filter(list, null));
        Assertions.assertFalse(action.filter(array, null));
    }

    @Test
    public void size_greater_equal() {
        FilterAction action = FilterAction.size_greater_equal;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertTrue(action.filter(str, str.length()));
        Assertions.assertTrue(action.filter(str, str.length() - 1));
        Assertions.assertFalse(action.filter(str, str.length() + 1));
        Assertions.assertTrue(action.filter(map, map.size()));
        Assertions.assertTrue(action.filter(map, map.size() - 1));
        Assertions.assertFalse(action.filter(map, map.size() + 1));
        Assertions.assertTrue(action.filter(list, list.size()));
        Assertions.assertTrue(action.filter(list, list.size() - 1));
        Assertions.assertFalse(action.filter(list, list.size() + 1));
        Assertions.assertTrue(action.filter(array, array.length));
        Assertions.assertTrue(action.filter(array, array.length - 1));
        Assertions.assertFalse(action.filter(array, array.length + 1));
    }

    @Test
    public void size_greater_than() {
        FilterAction action = FilterAction.size_greater_than;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertFalse(action.filter(str, str.length()));
        Assertions.assertTrue(action.filter(str, str.length() - 1));
        Assertions.assertFalse(action.filter(str, str.length() + 1));
        Assertions.assertFalse(action.filter(map, map.size()));
        Assertions.assertTrue(action.filter(map, map.size() - 1));
        Assertions.assertFalse(action.filter(map, map.size() + 1));
        Assertions.assertFalse(action.filter(list, list.size()));
        Assertions.assertTrue(action.filter(list, list.size() - 1));
        Assertions.assertFalse(action.filter(list, list.size() + 1));
        Assertions.assertFalse(action.filter(array, array.length));
        Assertions.assertTrue(action.filter(array, array.length - 1));
        Assertions.assertFalse(action.filter(array, array.length + 1));
    }

    @Test
    public void size_less_equal() {
        FilterAction action = FilterAction.size_less_equal;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertTrue(action.filter(str, str.length()));
        Assertions.assertFalse(action.filter(str, str.length() - 1));
        Assertions.assertTrue(action.filter(str, str.length() + 1));
        Assertions.assertTrue(action.filter(map, map.size()));
        Assertions.assertFalse(action.filter(map, map.size() - 1));
        Assertions.assertTrue(action.filter(map, map.size() + 1));
        Assertions.assertTrue(action.filter(list, list.size()));
        Assertions.assertFalse(action.filter(list, list.size() - 1));
        Assertions.assertTrue(action.filter(list, list.size() + 1));
        Assertions.assertTrue(action.filter(array, array.length));
        Assertions.assertFalse(action.filter(array, array.length - 1));
        Assertions.assertTrue(action.filter(array, array.length + 1));
    }

    @Test
    public void size_less_than() {
        FilterAction action = FilterAction.size_less_than;
        String str = "abc";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        List<String> list = List.of("1", "22", "333", "4444");
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Assertions.assertFalse(action.filter(str, str.length()));
        Assertions.assertFalse(action.filter(str, str.length() - 1));
        Assertions.assertTrue(action.filter(str, str.length() + 1));
        Assertions.assertFalse(action.filter(map, map.size()));
        Assertions.assertFalse(action.filter(map, map.size() - 1));
        Assertions.assertTrue(action.filter(map, map.size() + 1));
        Assertions.assertFalse(action.filter(list, list.size()));
        Assertions.assertFalse(action.filter(list, list.size() - 1));
        Assertions.assertTrue(action.filter(list, list.size() + 1));
        Assertions.assertFalse(action.filter(array, array.length));
        Assertions.assertFalse(action.filter(array, array.length - 1));
        Assertions.assertTrue(action.filter(array, array.length + 1));
    }

    @Test
    public void str_start_with() {
        FilterAction action = FilterAction.str_start_with;
        Assertions.assertTrue(action.filter("abc", "a"));
        Assertions.assertTrue(action.filter("abc", "ab"));
        Assertions.assertTrue(action.filter("abc", "abc"));
        Assertions.assertFalse(action.filter("abc", "abcd"));
    }

    @Test
    public void str_end_with() {
        FilterAction action = FilterAction.str_end_with;
        Assertions.assertTrue(action.filter("abc", "c"));
        Assertions.assertTrue(action.filter("abc", "bc"));
        Assertions.assertTrue(action.filter("abc", "abc"));
        Assertions.assertFalse(action.filter("abc", "abcd"));
    }

    @Test
    public void expression() {
        String exp = "value";
        FilterAction action = new ExpressionFilter(exp);
        Assertions.assertFalse(action.filter("abc", "c"));
        Assertions.assertFalse(action.filter("abc", "bc"));
        Assertions.assertTrue(action.filter("abc", "abc"));
        Assertions.assertFalse(action.filter("abc", "abcd"));

        exp = "value>10";
        action = new ExpressionFilter(exp);
        Assertions.assertFalse(action.filter(9, null));
        Assertions.assertFalse(action.filter(9.0, null));
        Assertions.assertFalse(action.filter(new BigDecimal(9), null));
        Assertions.assertFalse(action.filter(10, null));
        Assertions.assertFalse(action.filter(10.0, null));
        Assertions.assertFalse(action.filter(new BigDecimal(10), null));
        Assertions.assertTrue(action.filter(11, null));
        Assertions.assertTrue(action.filter(11.0, null));
        Assertions.assertTrue(action.filter(new BigDecimal(11), null));

        EngineContext context = new EngineContext("");
        exp = "value==data[key]";
        Map<String, Object> map = Map.of("a", "aaa", "b", "bbb", "c", "ccc");
        action = new ExpressionFilter(exp);
        Assertions.assertTrue(action.filter(context, map, "a", "aaa", null));
        Assertions.assertFalse(action.filter(context, map, "a", "a", null));
        Assertions.assertFalse(action.filter(context, map, "a", "a", true));
        Assertions.assertTrue(action.filter(context, map, "a", "a", false));
        Assertions.assertTrue(action.filter(context, map, "b", "bbb", null));
        Assertions.assertFalse(action.filter(context, map, "b", "bb", null));
        Assertions.assertFalse(action.filter(context, map, "b", "bb", true));
        Assertions.assertTrue(action.filter(context, map, "b", "bb", false));
    }

}
