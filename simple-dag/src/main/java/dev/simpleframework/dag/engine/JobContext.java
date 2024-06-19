package dev.simpleframework.dag.engine;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 作业上下文
 *
 * @author loyayz
 **/
public class JobContext {

    /**
     * 前置作业
     */
    private final Map<String, Boolean> froms;
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

    public JobContext() {
        this.froms = new LinkedHashMap<>();
        this.runningFroms = new ArrayList<>();
        this.countReceive = new AtomicLong(0);
        this.countEmit = new AtomicLong(0);
        this.beginTime = -1L;
        this.finishTime = -1L;
    }

    public Map<String, Boolean> froms() {
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

    public void initFroms(Collection<String> ids) {
        for (String id : ids) {
            this.froms.put(id, null);
        }
        this.runningFroms.addAll(ids);
    }

    public synchronized boolean removeRunning(String id, boolean success) {
        this.runningFroms.remove(id);
        this.froms.put(id, success);
        return this.runningFroms.isEmpty();
    }

    public long incrementReceive() {
        return this.countReceive.incrementAndGet();
    }

    public long incrementEmit() {
        return this.countEmit.incrementAndGet();
    }

    public void initBegin() {
        this.beginTime = System.currentTimeMillis();
    }

    public void setFinish() {
        this.finishTime = System.currentTimeMillis();
    }

}
