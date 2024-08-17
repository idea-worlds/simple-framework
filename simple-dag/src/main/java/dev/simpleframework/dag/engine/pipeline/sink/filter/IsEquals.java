package dev.simpleframework.dag.engine.pipeline.sink.filter;

import java.util.Objects;

/**
 * 相等
 *
 * @author loyayz
 **/
@SuppressWarnings("unchecked")
public class IsEquals extends AbstractFilterAction {

    @Override
    public boolean doFilter(Object value, Object expected) {
        if (value == null || !expected.getClass().isAssignableFrom(value.getClass())) {
            return false;
        }
        if (value instanceof Comparable v && expected instanceof Comparable e) {
            return v.compareTo(e) == 0;
        }
        return Objects.deepEquals(value, expected);
    }

}
