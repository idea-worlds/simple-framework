package dev.simpleframework.dag.engine;

import dev.simpleframework.util.Threads;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

/**
 * 引擎上下文
 *
 * @author loyayz
 **/
public class EngineContext {

    private final String name;
    private final List<JobContext> jobs = new ArrayList<>();
    private ThreadFactory threadFactory;

    public EngineContext(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public ThreadFactory threadFactory() {
        return this.threadFactory;
    }

    /**
     * 是否已完成
     */
    public boolean finished() {
        return this.jobs.stream()
                .allMatch(job -> job.status().isFinish());
    }

    /**
     * 获取正在执行的作业
     */
    public List<String> getRunningJobs() {
        return this.jobs.stream()
                .filter(job -> job.status() == RunStatus.RUNNING)
                .map(JobContext::id)
                .toList();
    }

    /**
     * 获取已完成的作业
     *
     * @return 作业 id, 是否正常
     */
    public Map<String, RunStatus> getFinishedJobs() {
        Map<String, RunStatus> result = new LinkedHashMap<>();
        for (JobContext job : this.jobs) {
            RunStatus status = job.status();
            if (status.isFinish()) {
                result.put(job.id(), status);
            }
        }
        return result;
    }

    void initThreads() {
        String groupName = "dag-" + this.name;
        this.threadFactory = Threads.newFactory(groupName);
    }

    void addJob(JobContext job) {
        this.jobs.add(job);
    }

}
