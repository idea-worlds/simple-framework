package dev.simpleframework.dag.engine.pipeline.sink.trans;

import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.TransAction;

/**
 * 值转换方法：函数
 *
 * @author loyayz
 **/
public class FunctionTrans implements TransAction {
    private final Function function;

    public FunctionTrans(Function function) {
        this.function = function;
    }

    @Override
    public Object trans(EngineContext context, Object data, Object key, Object value) {
        return this.function.apply(data, key, value);
    }

    @FunctionalInterface
    public interface Function {
        Object apply(Object data, Object key, Object value);
    }

}
