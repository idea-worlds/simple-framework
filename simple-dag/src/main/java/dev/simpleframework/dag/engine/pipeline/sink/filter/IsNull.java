package dev.simpleframework.dag.engine.pipeline.sink.filter;

/**
 * 为 null
 *
 * @author loyayz
 **/
public class IsNull extends AbstractFilterAction {

    @Override
    public boolean doFilter(Object value, Object expected) {
        return value == null;
    }

}
