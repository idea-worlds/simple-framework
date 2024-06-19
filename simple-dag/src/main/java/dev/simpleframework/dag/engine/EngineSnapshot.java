package dev.simpleframework.dag.engine;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 执行引擎运行时快照，存当前时刻的上下文信息
 *
 * @author loyayz
 **/
@Data
public class EngineSnapshot {

    /**
     * 作业运行时快照
     */
    private Map<String, JobSnapshot> jobs;
    /**
     * 是否运行
     */
    private Boolean running;
    /**
     * 开始时间：所有节点最早的开始时间
     */
    private Long beginTime;
    /**
     * 结束时间：所有节点最晚的结束时间
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

    public EngineSnapshot() {
        this.jobs = new LinkedHashMap<>();
    }

    public void jobSnapshot(String jobId, JobSnapshot jobSnapshot) {
        // 有作业在执行则表示引擎也还在执行中
        if (this.running == null || !this.running) {
            this.running = jobSnapshot.getRunning();
        }
        // 开始时间：所有节点最早的开始时间
        if (this.beginTime == null || this.beginTime > jobSnapshot.getBeginTime()) {
            this.beginTime = jobSnapshot.getBeginTime();
        }
        // 结束时间：所有节点最晚的结束时间
        if (!this.running) {
            if (this.finishTime == null || this.finishTime < jobSnapshot.getFinishTime()) {
                this.finishTime = jobSnapshot.getFinishTime();
            }
        }
        if (this.finishTime != null) {
            this.runTime = this.finishTime - this.beginTime;
        }
        this.jobs.put(jobId, jobSnapshot);
    }

}
