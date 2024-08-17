package dev.simpleframework.dag.engine.pipeline.sink.filter;

/**
 * 不为 null
 *
 * @author loyayz
 **/
public class NotNull extends AbstractFilterAction {

    @Override
    public boolean doFilter(Object value, Object expected) {
        return value != null;
    }

}
