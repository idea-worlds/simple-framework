package dev.simpleframework.dag.engine.pipeline.sink;

import dev.simpleframework.dag.engine.EngineContext;
import dev.simpleframework.dag.engine.pipeline.sink.filter.*;

/**
 * 数据校验方法
 *
 * @author loyayz
 **/
public interface FilterAction {

    /**
     * 数据校验
     *
     * @param context  上下文
     * @param data     要检验的数据
     * @param key      要检验的键
     * @param value    data.key 对应的值
     * @param expected 期望值。当它是 TransAction 时，会先执行转换方法。
     * @return 是否校验通过
     */
    boolean filter(EngineContext context, Object data, Object key, Object value, Object expected);

    default boolean filter(Object value, Object expected) {
        return this.filter(null, null, null, value, expected);
    }

    FilterAction always = new Always();
    FilterAction is_null = new IsNull();
    FilterAction not_null = new NotNull();
    FilterAction is_equals = new IsEquals();
    FilterAction not_equals = new NotEquals();
    FilterAction greater_than = new GreaterThan();
    FilterAction great_equal = new GreaterEqual();
    FilterAction less_than = new LessThan();
    FilterAction less_equal = new LessEqual();
    FilterAction is_empty = new IsEmpty();
    FilterAction not_empty = new NotEmpty();
    FilterAction is_contains_all = new IsContainsAll();
    FilterAction is_contains_any = new IsContainsAny();
    FilterAction is_contained_by = new IsContainedBy();
    FilterAction not_contains = new NotContains();
    FilterAction size_greater_equal = new SizeGreaterEqual();
    FilterAction size_greater_than = new SizeGreaterThan();
    FilterAction size_less_equal = new SizeLessEqual();
    FilterAction size_less_than = new SizeLessThan();
    FilterAction str_start_with = new StrStartWith();
    FilterAction str_end_with = new StrEndWith();

}
