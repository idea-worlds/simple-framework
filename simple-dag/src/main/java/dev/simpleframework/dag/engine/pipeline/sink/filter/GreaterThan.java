package dev.simpleframework.dag.engine.pipeline.sink.filter;

/**
 * 大于
 *
 * @author loyayz
 **/
public class GreaterThan extends AbstractFilterAction {

    @Override
    public boolean doFilter(Object value, Object expected) {
        if (value == null || !expected.getClass().isAssignableFrom(value.getClass())) {
            return false;
        }
        if (value instanceof Comparable v && expected instanceof Comparable e) {
            return v.compareTo(e) > 0;
        }
        return false;
    }

}
