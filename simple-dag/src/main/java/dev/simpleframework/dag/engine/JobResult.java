package dev.simpleframework.dag.engine;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 作业结果
 *
 * @author loyayz
 **/
@Data
public class JobResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * 作业 id
     */
    private String id;
    /**
     * 状态
     */
    private RunStatus status;
    /**
     * 接收的数据量
     */
    private Long countReceive;
    /**
     * 发送的数据量
     */
    private Long countEmit;
    /**
     * 开始时间
     */
    private Long beginTime;
    /**
     * 结束时间：完成或异常的时间
     */
    private Long finishTime;
    /**
     * 执行时间：结束时间 - 开始时间
     */
    private Long runTime;
    /**
     * 异常
     */
    private Throwable error;
    /**
     * 结果值
     */
    private Object value;

    public JobResult() {
    }

    public JobResult(String id) {
        this.id = id;
    }

    void fill(JobContext context) {
        this.status = context.status();
        this.countReceive = context.countReceive();
        this.countEmit = context.countEmit();
        this.beginTime = context.beginTime();
        this.finishTime = context.finishTime();
        long endTime = this.finishTime > 0 ? this.finishTime : System.currentTimeMillis();
        this.runTime = endTime - this.beginTime;
    }

    @SuppressWarnings("unchecked")
    public <T> T value() {
        return (T) this.value;
    }

}
