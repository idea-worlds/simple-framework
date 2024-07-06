package dev.simpleframework.dag.engine;

import dev.simpleframework.dag.DAG;
import dev.simpleframework.dag.DAGNode;
import dev.simpleframework.util.Clock;
import dev.simpleframework.util.Threads;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 引擎抽象类
 *
 * @param <J> 作业实现类
 * @author loyayz
 **/
public abstract class AbstractEngine<J extends AbstractJob> implements Engine<J> {
    private final DAG<J> dag;
    private final EngineContext context;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean aborted = new AtomicBoolean(false);

    public AbstractEngine(String name) {
        this.dag = new DAG<>();
        this.context = new EngineContext(name);
    }

    protected abstract J createVirtualBeginJob();

    @Override
    public Engine<J> addJob(J job) {
        DAGNode<J> node = new JobNode<>(job);
        this.dag.addNode(node);
        return this;
    }

    @Override
    public Engine<J> addEdge(String fromKey, String toKey) {
        this.dag.addEdge(fromKey, toKey);
        return this;
    }

    /**
     * 执行顺序：虚拟开始作业 -> 起始作业 -> 拓扑顺序作业... -> 结束作业
     */
    @Override
    public void execAsync(Consumer<EngineResult> action, long timeout, TimeUnit unit, TimeoutStrategy strategy) {
        if (!this.running.compareAndSet(false, true)) {
            throw new RuntimeException("This engine is already running.");
        }
        this.aborted.set(false);
        this.dag.sort();
        this.context.initThreads();

        // 按拓扑顺序调用所有作业的初始化方法，并按依赖订阅
        this.dag.visit(job -> {
            this.context.addJob(job.context());
            job.engineContext(this.context);
            job.doInit();
        }, AbstractJob::start, true);

        // 实际起始节点订阅虚拟起始节点
        J begin = this.createVirtualBeginJob();
        J start = this.dag.getStartNode().getValue();
        begin.engineContext(this.context);
        start.listen(begin);

        Threads.newPool(this.context.threadFactory(), 1, 1).submit(() -> {
            // 虚拟起始节点发送完成信号
            begin.start();
            begin.emitComplete();
            // 等待执行结束
            EngineResult result = this.waitResult(timeout, unit, strategy);
            action.accept(result);
        });
    }

    @Override
    public void abort() {
        this.dag.visit(AbstractJob::abort, false);
        this.aborted.set(true);
    }

    @Override
    public EngineSnapshot snapshot() {
        EngineSnapshot snapshot = new EngineSnapshot();
        snapshot.setName(this.context.name());
        this.dag.visit(job -> snapshot.addJob(job.snapshot()), false);
        return snapshot;
    }

    private EngineResult waitResult(long timeout, TimeUnit unit, TimeoutStrategy strategy) {
        if (strategy == null) {
            // 默认超时后什么都不做
            strategy = TimeoutStrategy.NOTHING;
        }
        DelayQueue<DelayValue> queue = new DelayQueue<>();
        if (timeout > 0) {
            // 有超时时间，用延迟队列到时再取结果
            queue.offer(new DelayValue(timeout, unit, this.aborted));
            try {
                queue.take();
            } catch (Exception ignore) {
            }
        } else {
            // 无超时时间，每隔200ms判断是否已结束
            Threads.sleep(200);
            while (!this.context.finished()) {
                queue.offer(new DelayValue(200, TimeUnit.MILLISECONDS, this.aborted));
                try {
                    queue.take();
                } catch (Exception ignore) {
                }
            }
        }
        if (this.context.finished()) {
            // 超时时间内已完成，执行作业的清理方法
            this.dag.visit(AbstractJob::clear, false);
        } else {
            // 超时时间内未完成，执行策略方法
            strategy.exec(this);
            // 起一守护线程定时判断是否已完成，已完成后执行作业的清理方法
            Thread thread = new Thread(() -> {
                while (!this.context.finished()) {
                    Threads.sleep(1000);
                }
                this.dag.visit(AbstractJob::clear, false);
            });
            thread.setName("dag-clear-" + this.context.name());
            thread.setDaemon(true);
            thread.start();
        }
        EngineResult result = new EngineResult();
        result.setName(this.context.name());
        this.dag.visit(job -> result.addJob(job.result()), false);
        return result;
    }

    static class JobNode<J extends AbstractJob> extends DAGNode<J> {
        JobNode(J job) {
            super(job.id(), job);
            super.setVisitHandler((c, froms, tos) -> job.listen(froms));
        }
    }

    static class DelayValue implements Delayed {
        private final Long time;
        private final AtomicBoolean aborted;

        DelayValue(long delay, TimeUnit unit, AtomicBoolean aborted) {
            this.time = Clock.now() + unit.toMillis(delay);
            this.aborted = aborted;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            if (this.aborted.get()) {
                return 0;
            }
            long diff = this.time - Clock.now();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return this.time.compareTo(((DelayValue) o).time);
        }

    }

}
