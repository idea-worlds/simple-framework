package dev.simpleframework.dag.engine.pipeline.sink.filter;

/**
 * 以字符串结尾
 *
 * @author loyayz
 **/
public class StrEndWith extends AbstractFilterAction {

    @Override
    public boolean doFilter(Object value, Object expected) {
        return expected != null
                && value instanceof String
                && value.toString().endsWith(expected.toString());
    }

}
