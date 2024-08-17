package dev.simpleframework.dag.engine.pipeline.sink.filter;

import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.FilterAction;
import dev.simpleframework.dag.engine.pipeline.sink.TransAction;

/**
 * @author loyayz
 **/
public abstract class AbstractFilterAction implements FilterAction {

    protected abstract boolean doFilter(Object value, Object expected);

    protected boolean doFilter(EngineContext context, Object data, Object key, Object value, Object expected) {
        return this.doFilter(value, expected);
    }

    @Override
    public boolean filter(EngineContext context, Object data, Object key, Object value, Object expected) {
        if (expected instanceof TransAction trans) {
            expected = trans.trans(context, data, key, value);
        }
        return this.doFilter(context, data, key, value, expected);
    }

}
