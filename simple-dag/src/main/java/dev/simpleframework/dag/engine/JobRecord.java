package dev.simpleframework.dag.engine;

import dev.simpleframework.util.Clock;
import lombok.Getter;
import lombok.ToString;

/**
 * 上下文数据
 *
 * @author loyayz
 **/
@Getter
@ToString
public class JobRecord {

    /**
     * 生成数据的作业
     */
    private final String source;
    /**
     * 发送数据的作业
     */
    private final String from;
    /**
     * 数据时间：为提升性能，此时间取当前时间近似值
     */
    private final long time;
    /**
     * 数据值
     */
    private final Object data;

    public JobRecord(String source, Object data) {
        this(source, source, data);
    }

    public JobRecord(String source, String from, Object data) {
        this.source = source;
        this.from = from;
        this.time = Clock.now();
        this.data = data;
    }

}
