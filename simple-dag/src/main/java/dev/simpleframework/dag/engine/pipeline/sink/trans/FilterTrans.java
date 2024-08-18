package dev.simpleframework.dag.engine.pipeline.sink.trans;

import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.FilterAction;
import dev.simpleframework.dag.engine.pipeline.sink.TransAction;

import java.util.ArrayList;
import java.util.List;

/**
 * 值转换方法：根据校验转为新值
 *
 * @author loyayz
 **/
public class FilterTrans implements TransAction {
    private final List<Args> args;

    public static FilterTrans of() {
        return new FilterTrans();
    }

    private FilterTrans() {
        this.args = new ArrayList<>();
    }

    /**
     * 添加转换方法
     *
     * @param filterAction   校验方法
     * @param filterExpected 期望值。当它是 TransAction 时，会先执行转换方法。
     * @param transAction    新值
     * @return this
     */
    public FilterTrans addTrans(FilterAction filterAction, Object filterExpected, TransAction transAction) {
        this.args.add(new Args(filterAction, filterExpected, transAction));
        return this;
    }

    @Override
    public Object trans(EngineContext context, Object data, Object key, Object value) {
        for (Args arg : this.args) {
            if (arg.filterAction.filter(context, data, key, value, arg.filterExpected)) {
                return arg.transAction.trans(context, data, key, value);
            }
        }
        return null;
    }

    private record Args(FilterAction filterAction, Object filterExpected, TransAction transAction) {
    }

}
