package dev.simpleframework.dag.engine;

import lombok.Data;

/**
 * 作业运行时快照，存当前时刻的上下文信息
 *
 * @author loyayz
 **/
@Data
public class JobSnapshot {

    private String id;
    /**
     * 状态
     */
    private RunStatus status;
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
        this.id = context.id();
        this.status = context.status();
        this.countReceive = context.countReceive();
        this.countEmit = context.countEmit();
        this.beginTime = context.beginTime();
        this.finishTime = context.finishTime();
        this.runTime = result.getRunTime();
        this.error = result.getError();
        this.value = result.getValue();
    }

}
