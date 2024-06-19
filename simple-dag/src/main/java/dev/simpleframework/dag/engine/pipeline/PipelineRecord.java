package dev.simpleframework.dag.engine.pipeline;

import dev.simpleframework.dag.util.Clock;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 上下文数据
 *
 * @author loyayz
 **/
@Data
public class PipelineRecord implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * 数据产生时间：为提升性能，此时间取当前时间近似值
     */
    private long time;
    /**
     * 当前数据：不可变
     */
    private Map<String, Object> data;

    public PipelineRecord() {
        this.time = Clock.now();
        this.data = Collections.emptyMap();
    }

    public PipelineRecord(Map<String, Object> data) {
        this.time = Clock.now();
        this.data = Collections.unmodifiableMap(data);
    }

}
