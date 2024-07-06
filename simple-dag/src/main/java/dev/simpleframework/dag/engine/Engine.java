package dev.simpleframework.dag.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 有向无环图执行引擎
 *
 * @author loyayz
 **/
public interface Engine<T extends Job> {

    /**
     * 添加作业
     */
    Engine<T> addJob(T job);

    /**
     * 添加作业
     */
    default Engine<T> addJob(Collection<T> jobs) {
        jobs.forEach(this::addJob);
        return this;
    }

    /**
     * 添加边（添加完所有作业再添加边）
     *
     * @param fromKey 来源作业
     * @param toKey   目标作业
     */
    Engine<T> addEdge(String fromKey, String toKey);

    /**
     * 添加边（添加完所有作业再添加边）
     *
     * @param fromKey 来源作业
     * @param toKeys  目标作业
     */
    default Engine<T> addEdge(String fromKey, List<String> toKeys) {
        toKeys.forEach(toKey -> this.addEdge(fromKey, toKey));
        return this;
    }

    /**
     * 执行
     */
    default EngineResult exec() {
        return this.exec(-1L, TimeUnit.MILLISECONDS, null);
    }

    /**
     * 执行
     *
     * @param timeout 超时时间，-1 表示不限时
     * @param unit    超时时间单位
     */
    default EngineResult exec(long timeout, TimeUnit unit) {
        return this.exec(timeout, unit, TimeoutStrategy.NOTHING);
    }

    /**
     * 执行
     *
     * @param timeout  超时时间，-1 表示不限时
     * @param unit     超时时间单位
     * @param strategy 超时策略
     */
    default EngineResult exec(long timeout, TimeUnit unit, TimeoutStrategy strategy) {
        List<EngineResult> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        this.execAsync(r -> {
            result.add(r);
            latch.countDown();
        }, timeout, unit, strategy);
        try {
            latch.await();
        } catch (Exception ignore) {
        }
        return result.get(0);
    }

    /**
     * 异步执行
     *
     * @param action 执行结束后回调
     */
    default void execAsync(Consumer<EngineResult> action) {
        this.execAsync(action, -1L, TimeUnit.MILLISECONDS, null);
    }

    /**
     * 异步执行
     *
     * @param action  执行结束后回调
     * @param timeout 超时时间，-1 表示不限时
     * @param unit    超时时间单位
     */
    void execAsync(Consumer<EngineResult> action, long timeout, TimeUnit unit, TimeoutStrategy strategy);

    /**
     * 中止执行
     */
    void abort();

    /**
     * 获取当前运行时快照
     */
    EngineSnapshot snapshot();

    /**
     * 超时策略
     */
    enum TimeoutStrategy {
        /**
         * 什么都不做
         */
        NOTHING,
        /**
         * 中止执行
         */
        ABORT {
            @Override
            void exec(Engine<?> engine) {
                engine.abort();
            }
        };

        void exec(Engine<?> engine) {
        }

    }

}
