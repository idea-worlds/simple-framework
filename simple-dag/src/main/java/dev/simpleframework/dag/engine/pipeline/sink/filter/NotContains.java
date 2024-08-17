package dev.simpleframework.dag.engine.pipeline.sink.filter;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 不包含，只判断 string、map key、collection、array
 *
 * @author loyayz
 **/
public class NotContains extends AbstractCollectionFilterAction {

    @Override
    protected boolean filterString(String value, Set<Object> expected) {
        if (value.isEmpty()) {
            return !expected.isEmpty();
        }
        if (expected.isEmpty()) {
            return true;
        }
        for (Object o : expected) {
            if (value.contains(o.toString())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean filterMap(Map<?, ?> value, Set<Object> expected) {
        if (value.isEmpty()) {
            return !expected.isEmpty();
        }
        if (expected.isEmpty()) {
            return true;
        }
        return !value.keySet().containsAll(expected);
    }

    @Override
    protected boolean filterCollection(Collection<?> value, Set<Object> expected) {
        if (value.isEmpty()) {
            return !expected.isEmpty();
        }
        if (expected.isEmpty()) {
            return true;
        }
        return !value.containsAll(expected);
    }

    @Override
    protected boolean filterArray(Object[] value, Set<Object> expected) {
        if (value.length == 0) {
            return !expected.isEmpty();
        }
        if (expected.isEmpty()) {
            return true;
        }
        return !expected.stream().allMatch(e -> {
            for (Object d : value) {
                if (Objects.deepEquals(d, e)) {
                    return true;
                }
            }
            return false;
        });
    }

}
