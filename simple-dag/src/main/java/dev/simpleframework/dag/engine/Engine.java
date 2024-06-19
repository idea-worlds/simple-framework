package dev.simpleframework.dag.engine;

import java.util.Collection;
import java.util.List;

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
    void exec();

    /**
     * 获取当前运行时快照
     */
    EngineSnapshot snapshot();

}
