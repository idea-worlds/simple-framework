package dev.simpleframework.dag.engine.pipeline.sink.trans;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.TransAction;

import java.util.HashMap;
import java.util.Map;

/**
 * 值转换方法：表达式
 *
 * @author loyayz
 **/
public class ExpressionTrans implements TransAction {
    private final Expression expression;

    public ExpressionTrans(String expression) {
        this.expression = AviatorEvaluator.compile(expression);
    }

    @Override
    public Object trans(EngineContext context, Object data, Object key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("env", context.envs());
        params.put("data", data);
        params.put("key", key);
        params.put("value", value);
        return this.expression.execute(params);
    }

}
