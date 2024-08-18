package dev.simpleframework.dag.engine.pipeline.sink.trans;

import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.TransAction;

/**
 * 值转换方法：变量
 *
 * @author loyayz
 **/
public class EnvTrans implements TransAction {
    private final String envKey;
    private final boolean byValue;

    public static EnvTrans byConstantKey(String envKey) {
        return new EnvTrans(envKey, false);
    }

    public static EnvTrans byDynamicValue(String group) {
        return new EnvTrans(group, true);
    }

    private EnvTrans(String key, boolean byValue) {
        this.envKey = key;
        this.byValue = byValue;
    }

    @Override
    public Object trans(EngineContext context, Object data, Object key, Object value) {
        if (this.byValue) {
            return value == null ? null : context.getEnv(this.envKey + "." + value);
        } else {
            return context.getEnv(this.envKey);
        }
    }

}
