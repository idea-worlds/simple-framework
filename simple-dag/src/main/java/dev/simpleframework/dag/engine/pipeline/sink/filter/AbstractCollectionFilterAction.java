package dev.simpleframework.dag.engine.pipeline.sink.filter;

import java.util.*;

/**
 * @author loyayz
 **/
public abstract class AbstractCollectionFilterAction extends AbstractFilterAction {

    protected abstract boolean filterString(String value, Set<Object> expected);

    protected abstract boolean filterMap(Map<?, ?> value, Set<Object> expected);

    protected abstract boolean filterCollection(Collection<?> value, Set<Object> expected);

    protected abstract boolean filterArray(Object[] value, Set<Object> expected);

    @Override
    public boolean doFilter(Object value, Object expected) {
        Set<Object> expectedValues = this.parseExpectedValues(expected);
        if (expectedValues == null) {
            return false;
        }
        if (value instanceof String v) {
            return this.filterString(v, expectedValues);
        }
        if (value instanceof Map<?, ?> v) {
            return this.filterMap(v, expectedValues);
        }
        if (value instanceof Collection<?> v) {
            return this.filterCollection(v, expectedValues);
        }
        if (value.getClass().isArray()) {
            return this.filterArray((Object[]) value, expectedValues);
        }
        return false;
    }

    protected Set<Object> parseExpectedValues(Object expected) {
        if (expected == null) {
            return null;
        }
        Set<Object> expectedValues = new HashSet<>();
        if (expected instanceof Collection<?> v) {
            expectedValues.addAll(v);
        } else if (expected.getClass().isArray()) {
            expectedValues.addAll(Arrays.asList((Object[]) expected));
        } else {
            expectedValues.add(expected);
        }
        return expectedValues;
    }

}
