package dev.simpleframework.dag.engine.pipeline.sink.filter;

/**
 * 以字符串起始
 *
 * @author loyayz
 **/
public class StrStartWith extends AbstractFilterAction {

    @Override
    public boolean doFilter(Object value, Object expected) {
        return expected != null
                && value instanceof String
                && value.toString().startsWith(expected.toString());
    }

}
