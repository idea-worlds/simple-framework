package dev.simpleframework.dag.engine.pipeline.sink.filter;

/**
 * @author loyayz
 **/
public class Always extends AbstractFilterAction {

    @Override
    public boolean doFilter(Object value, Object expected) {
        return true;
    }

}
