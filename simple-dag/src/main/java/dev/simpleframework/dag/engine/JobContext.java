package dev.simpleframework.dag.engine;

import java.util.*;
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
    private final AtomicLong countReceive;
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
        this.countReceive = new AtomicLong(0);
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

    public long countReceive() {
        return this.countReceive.get();
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

    public synchronized boolean decrementRunning(String id, RunStatus status) {
        this.runningFroms.remove(id);
        this.froms.put(id, status);
        return this.runningFroms.isEmpty();
    }

    public void incrementReceive() {
        this.countReceive.incrementAndGet();
    }

    public void incrementEmit() {
        this.countEmit.incrementAndGet();
    }

    void initBegin(Collection<String> ids) {
        this.froms.clear();
        this.runningFroms.clear();
        for (String id : ids) {
            this.froms.put(id, RunStatus.RUNNING);
        }
        this.runningFroms.addAll(ids);
        this.status = RunStatus.RUNNING;
        this.beginTime = System.currentTimeMillis();
    }

    void setFinish(Throwable error) {
        this.finishTime = System.currentTimeMillis();
        if (error == null) {
            this.setStatus(RunStatus.COMPLETE);
        } else {
            this.setStatus(RunStatus.FAIL);
        }
    }

    void setStatus(RunStatus status) {
        this.status = status;
    }

    void abort() {
        if (this.status.isFinish()) {
            return;
        }
        this.finishTime = System.currentTimeMillis();
        this.setStatus(RunStatus.ABORT);
    }

}
