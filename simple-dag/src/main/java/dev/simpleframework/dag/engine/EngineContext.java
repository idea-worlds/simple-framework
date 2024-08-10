package dev.simpleframework.dag.engine;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import dev.simpleframework.util.Threads;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * 引擎上下文
 *
 * @author loyayz
 **/
@SuppressWarnings("unchecked")
public class EngineContext {
    private final String name;
    private final ThreadFactory threadFactory;
    /**
     * 变量
     */
    private final Map<String, Object> envs = new ConcurrentHashMap<>();
    /**
     * 作业
     */
    private final List<JobContext> jobs = new ArrayList<>();

    public EngineContext(String name) {
        this.name = name;
        this.threadFactory = Threads.newFactory("dag-" + this.name);
        this.envs.put("_engine_name", name);
    }

    public String name() {
        return this.name;
    }

    /**
     * 线程
     */
    public ThreadFactory threadFactory() {
        return this.threadFactory;
    }

    /**
     * 获取变量
     */
    public Map<String, Object> envs() {
        return new LinkedHashMap<>(this.envs);
    }

    /**
     * 获取变量
     *
     * @param key 变量 key
     */
    public <T> T getEnv(String key) {
        T env = (T) this.envs.get(key);
        if (env == null) {
            boolean isExp = key.contains(".") ||
                    (key.contains("[") && key.contains("]"));
            if (isExp) {
                Expression expression = AviatorEvaluator.getCachedExpression(key);
                if (expression == null) {
                    expression = AviatorEvaluator.compile(key, true);
                }
                env = (T) expression.execute(this.envs());
            }
        }
        return env;
    }

    /**
     * 设置变量
     */
    public void setEnv(String group, String key, Object value) {
        if (group == null || group.isBlank()) {
            this.envs.put(key, value);
        } else {
            Map<String, Object> env = (Map<String, Object>) this.envs.computeIfAbsent(group, k -> new ConcurrentHashMap<>());
            env.put(key, value);
        }
    }

    /**
     * 是否已完成
     */
    public boolean isFinished() {
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
     * @return 作业 id, 状态
     */
    public Map<String, RunStatus> getFinishedJobs() {
        Map<String, RunStatus> result = new LinkedHashMap<>();
        this.jobs.stream()
                .filter(job -> job.status().isFinish())
                .forEach(job -> result.put(job.id(), job.status()));
        return result;
    }

    void addJob(JobContext job) {
        this.jobs.add(job);
    }

}
