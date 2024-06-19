package dev.simpleframework.dag.engine;

import lombok.Data;

/**
 * 作业运行时快照，存当前时刻的上下文信息
 *
 * @author loyayz
 **/
@Data
public class JobSnapshot {

    /**
     * 是否运行
     */
    private Boolean running;
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

    JobSnapshot() {
    }

    JobSnapshot(JobContext context, JobResult result) {
        this.countReceive = context.countReceive();
        this.countEmit = context.countEmit();
        this.beginTime = context.beginTime();
        this.finishTime = context.finishTime();
        this.runTime = result.getRunTime();
        this.success = result.getSuccess();
        this.error = result.getError();
        this.value = result.getValue();
        this.running = this.runTime != null && this.runTime >= 0L;
    }

}
