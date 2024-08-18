package dev.simpleframework.dag.engine.pipeline.sink.trans;

import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.TransAction;

/**
 * 值转换方法：常量
 *
 * @author loyayz
 **/
public class ConstantTrans implements TransAction {
    private final Object value;

    public ConstantTrans(Object value) {
        this.value = value;
    }

    @Override
    public Object trans(EngineContext context, Object data, Object key, Object value) {
        return this.value;
    }

}
