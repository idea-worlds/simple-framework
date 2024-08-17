package dev.simpleframework.dag.engine.pipeline.sink.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 被包含，只判断 string、map key、collection、array
 *
 * @author loyayz
 **/
public class IsContainedBy extends AbstractCollectionFilterAction {

    @Override
    protected boolean filterString(String value, Set<Object> expected) {
        if (value.isEmpty()) {
            return true;
        }
        if (expected.isEmpty()) {
            return false;
        }
        for (Object o : expected) {
            if (o.toString().contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean filterMap(Map<?, ?> value, Set<Object> expected) {
        if (value.isEmpty()) {
            return true;
        }
        if (expected.isEmpty()) {
            return false;
        }
        Collection<?> values = value.keySet();
        return expected.containsAll(values);
    }

    @Override
    protected boolean filterCollection(Collection<?> value, Set<Object> expected) {
        if (value.isEmpty()) {
            return true;
        }
        if (expected.isEmpty()) {
            return false;
        }
        return expected.containsAll(value);
    }

    @Override
    protected boolean filterArray(Object[] value, Set<Object> expected) {
        if (value.length == 0) {
            return true;
        }
        return expected.containsAll(Arrays.asList(value));
    }

}
