package dev.simpleframework.dag.engine.pipeline.sink.filter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 长度小于，只判断 string、map key、collection、array
 *
 * @author loyayz
 **/
public class SizeLessThan extends AbstractCollectionFilterAction {

    @Override
    protected boolean filterString(String value, Set<Object> expected) {
        return this.filterSize(value.length(), expected);
    }

    @Override
    protected boolean filterMap(Map<?, ?> value, Set<Object> expected) {
        return this.filterSize(value.size(), expected);
    }

    @Override
    protected boolean filterCollection(Collection<?> value, Set<Object> expected) {
        return this.filterSize(value.size(), expected);
    }

    @Override
    protected boolean filterArray(Object[] value, Set<Object> expected) {
        return this.filterSize(value.length, expected);
    }

    public boolean filterSize(int value, Set<Object> expected) {
        if (expected.isEmpty()) {
            return false;
        }
        Object[] array = expected.toArray();
        return value < Integer.parseInt(array[0].toString());
    }

}
