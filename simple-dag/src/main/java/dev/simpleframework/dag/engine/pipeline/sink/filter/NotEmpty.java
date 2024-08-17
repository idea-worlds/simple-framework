package dev.simpleframework.dag.engine.pipeline.sink.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 不为空，只判断 string、map、collection、array
 *
 * @author loyayz
 **/
public class NotEmpty extends AbstractCollectionFilterAction {

    @Override
    protected boolean filterString(String value, Set<Object> expected) {
        return !value.isEmpty();
    }

    @Override
    protected boolean filterMap(Map<?, ?> value, Set<Object> expected) {
        return !value.isEmpty();
    }

    @Override
    protected boolean filterCollection(Collection<?> value, Set<Object> expected) {
        return !value.isEmpty();
    }

    @Override
    protected boolean filterArray(Object[] value, Set<Object> expected) {
        return value.length > 0;
    }

    @Override
    protected Set<Object> parseExpectedValues(Object expected) {
        return Collections.emptySet();
    }

}
