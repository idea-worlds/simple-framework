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
     * 是否成功结束
     */
    private Boolean success;
    /**
     * 异常
     */
    private Throwable error;
    /**
     * 结果值
     */
    private Object value;

    public JobResult() {
        this.success = false;
    }

    public JobResult(String id) {
        this.id = id;
        this.success = false;
    }

    public void fill(JobContext context) {
        this.countReceive = context.countReceive();
        this.countEmit = context.countEmit();
        this.beginTime = context.beginTime();
        this.finishTime = context.finishTime();
        this.runTime = this.finishTime - this.beginTime;
    }

}
