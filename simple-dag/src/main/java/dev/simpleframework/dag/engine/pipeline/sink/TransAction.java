package dev.simpleframework.dag.engine.pipeline.sink;

import dev.simpleframework.dag.engine.EngineContext;

/**
 * 值转换方法
 *
 * @author loyayz
 **/
public interface TransAction {

    /**
     * 转换
     *
     * @param context 上下文
     * @param data    数据值
     * @param key     要转换的键
     * @param value   data.key 对应的值
     * @return
     */
    Object trans(EngineContext context, Object data,  Object key, Object value);

}
