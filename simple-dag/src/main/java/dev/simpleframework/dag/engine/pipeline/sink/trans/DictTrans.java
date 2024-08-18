package dev.simpleframework.dag.engine.pipeline.sink.trans;

import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.TransAction;

import java.util.HashMap;
import java.util.Map;

/**
 * 值转换方法：根据数据字典转为新值
 *
 * @author loyayz
 **/
public class DictTrans implements TransAction {
    /**
     * 数据字典
     */
    private final Map<Object, Object> dict;

    public static DictTrans of() {
        return new DictTrans();
    }

    private DictTrans() {
        this.dict = new HashMap<>();
    }

    public DictTrans addDict(Object dictKey, Object dictValue) {
        this.dict.put(dictKey, dictValue);
        return this;
    }

    @Override
    public Object trans(EngineContext context, Object data, Object key, Object value) {
        return value == null ? null : this.dict.get(value);
    }

}
