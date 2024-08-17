package dev.simpleframework.dag.engine.pipeline.sink.filter;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 包含任一个，只判断 string、map key、collection、array
 *
 * @author loyayz
 **/
public class IsContainsAny extends AbstractCollectionFilterAction {

    @Override
    protected boolean filterString(String value, Set<Object> expected) {
        if (value.isEmpty()) {
            return expected.isEmpty();
        }
        if (expected.isEmpty()) {
            return true;
        }
        for (Object o : expected) {
            if (value.contains(o.toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean filterMap(Map<?, ?> value, Set<Object> expected) {
        if (value.isEmpty()) {
            return expected.isEmpty();
        }
        if (expected.isEmpty()) {
            return true;
        }
        Collection<?> values = value.keySet();
        return expected.stream().anyMatch(values::contains);
    }

    @Override
    protected boolean filterCollection(Collection<?> value, Set<Object> expected) {
        if (value.isEmpty()) {
            return expected.isEmpty();
        }
        if (expected.isEmpty()) {
            return true;
        }
        return expected.stream().anyMatch(value::contains);
    }

    @Override
    protected boolean filterArray(Object[] value, Set<Object> expected) {
        if (value.length == 0) {
            return expected.isEmpty();
        }
        if (expected.isEmpty()) {
            return true;
        }
        return expected.stream().anyMatch(e -> {
            for (Object d : value) {
                if (Objects.deepEquals(d, e)) {
                    return true;
                }
            }
            return false;
        });
    }

}
