package dev.simpleframework.dag.engine.pipeline.sink.filter;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.FilterAction;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 表达式
 *
 * @author loyayz
 **/
public class ExpressionFilter extends AbstractFilterAction {
    private final Expression expression;

    public ExpressionFilter(String expression) {
        this.expression = AviatorEvaluator.compile(expression);
    }

    @Override
    protected boolean doFilter(Object value, Object expected) {
        return this.doFilter(null, null, null, value, expected);
    }

    @Override
    public boolean doFilter(EngineContext context, Object data, Object key, Object value, Object expected) {
        Map<String, Object> params = new HashMap<>();
        params.put("env", context == null ? Collections.emptyMap() : context.envs());
        params.put("data", data);
        params.put("key", key);
        params.put("value", value);
        Object result = this.expression.execute(params);
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean r) {
            boolean expectedValue;
            try {
                expectedValue = (boolean) expected;
            } catch (Exception e) {
                expectedValue = true;
            }
            return Boolean.compare(r, expectedValue) == 0;
        }
        if (result instanceof Number) {
            BigDecimal expectedValue;
            try {
                expectedValue = new BigDecimal(expected.toString());
            } catch (Exception e) {
                return false;
            }
            return new BigDecimal(String.valueOf(result)).compareTo(expectedValue) == 0;
        }
        if (result instanceof String r) {
            return expected != null && r.equals(expected.toString());
        }
        return false;
    }

}
