package dev.simpleframework.dag.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 作业上下文
 *
 * @author loyayz
 **/
public class JobContext {

    private final String id;
    /**
     * 状态
     */
    private volatile RunStatus status;
    /**
     * 前置作业
     */
    private final Map<String, RunStatus> froms;
    /**
     * 正在运行的前置作业
     */
    private final List<String> runningFroms;
    /**
     * 接收的数据量
     */
    private final Map<String, AtomicLong> countReceives;
    /**
     * 发送的数据量
     */
    private final AtomicLong countEmit;
    /**
     * 开始时间
     */
    private Long beginTime;
    /**
     * 结束时间：完成或异常的时间
     */
    private Long finishTime;

    public JobContext(String id) {
        this.id = id;
        this.status = RunStatus.WAIT;
        this.froms = new LinkedHashMap<>();
        this.runningFroms = new CopyOnWriteArrayList<>();
        this.countReceives = new ConcurrentHashMap<>();
        this.countEmit = new AtomicLong(0);
        this.beginTime = -1L;
        this.finishTime = -1L;
    }

    public String id() {
        return this.id;
    }

    public RunStatus status() {
        return this.status;
    }

    public Map<String, RunStatus> froms() {
        return Collections.unmodifiableMap(this.froms);
    }

    public Map<String, Long> countReceives() {
        Map<String, Long> result = new LinkedHashMap<>();
        this.countReceives.forEach((from, count) -> result.put(from, count.get()));
        return result;
    }

    public long countEmit() {
        return this.countEmit.get();
    }

    public long beginTime() {
        return this.beginTime;
    }

    public long finishTime() {
        return this.finishTime;
    }

    public synchronized boolean decrementRunning(String from, RunStatus status) {
        this.runningFroms.remove(from);
        this.froms.put(from, status);
        return this.runningFroms.isEmpty();
    }

    public void incrementReceive(String from) {
        this.countReceives.get(from).incrementAndGet();
    }

    public void incrementEmit() {
        this.countEmit.incrementAndGet();
    }

    void setFroms(Collection<String> froms) {
        this.froms.clear();
        this.runningFroms.clear();
        for (String id : froms) {
            this.froms.put(id, RunStatus.RUNNING);
            this.countReceives.put(id, new AtomicLong(0));
        }
        this.runningFroms.addAll(froms);
    }

    void setStatus(RunStatus status) {
        this.status = status;
        if (status == RunStatus.RUNNING && this.beginTime <= 0L) {
            this.beginTime = System.currentTimeMillis();
        } else if (status.isFinish()) {
            this.finishTime = System.currentTimeMillis();
        }
    }

}
