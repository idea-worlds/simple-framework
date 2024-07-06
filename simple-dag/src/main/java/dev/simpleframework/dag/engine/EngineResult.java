package dev.simpleframework.dag.engine;

import dev.simpleframework.util.Strings;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 引擎执行结果
 *
 * @author loyayz
 **/
@Data
public class EngineResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

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
     * 执行时间：结束时间 - 开始时间
     */
    private Long runTime;
    /**
     * 作业结果
     */
    private Map<String, JobResult> jobs;

    public EngineResult() {
        this.status = RunStatus.WAIT;
        this.jobs = new LinkedHashMap<>();
    }

    void addJob(JobResult job) {
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

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("EngineResult(\n" +
                "  name: " + this.name + "\n" +
                "  status: " + this.status + "\n" +
                "  beginTime: " + this.beginTime + "\n" +
                "  finishTime: " + this.finishTime + "\n" +
                "  runTime: " + Strings.readableTime(this.runTime) + "\n" +
                "  jobs: \n");
        for (JobResult job : jobs.values()) {
            str.append("    ").append(job.toString()).append("\n");
        }
        str.append(")");
        return str.toString();
    }

}
