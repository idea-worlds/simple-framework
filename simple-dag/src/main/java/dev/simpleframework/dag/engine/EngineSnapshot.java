package dev.simpleframework.dag.engine;

import dev.simpleframework.util.Strings;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 运行时快照，存当前时刻的上下文信息
 *
 * @author loyayz
 **/
@Data
public class EngineSnapshot {

    private String name;
    /**
     * 状态
     */
    private RunStatus status;
    /**
     * 开始时间：所有节点最早的开始时间
     */
    private Long beginTime;
    /**
     * 结束时间：所有节点最晚的结束时间
     */
    private Long finishTime;
    /**
     * 执行时间：( 结束时间 - 开始时间 ) 或 ( 结束时间 - 开始时间 )
     */
    private Long runTime;
    /**
     * 作业运行时快照
     */
    private Map<String, JobSnapshot> jobs;

    public EngineSnapshot() {
        this.status = RunStatus.WAIT;
        this.jobs = new LinkedHashMap<>();
    }

    /**
     * 获取可读格式的执行时间
     */
    public String readableRunTime() {
        return Strings.readableTime(this.runTime);
    }

    void addJob(JobSnapshot job) {
        RunStatus jobStatus = job.getStatus();
        if (this.status == RunStatus.WAIT || jobStatus == RunStatus.RUNNING) {
            this.status = jobStatus;
        } else if (this.status == RunStatus.COMPLETE && jobStatus == RunStatus.FAIL) {
            this.status = jobStatus;
        }
        // 开始时间：所有节点最早的开始时间
        if (this.beginTime == null || this.beginTime > job.getBeginTime()) {
            this.beginTime = job.getBeginTime();
        }
        // 结束时间：所有节点最晚的结束时间
        if (this.finishTime == null || this.finishTime < job.getFinishTime()) {
            this.finishTime = job.getFinishTime();
        }
        long endTime = this.finishTime != null ? this.finishTime : System.currentTimeMillis();
        this.runTime = endTime - this.beginTime;
        this.jobs.put(job.getId(), job);
    }

}
