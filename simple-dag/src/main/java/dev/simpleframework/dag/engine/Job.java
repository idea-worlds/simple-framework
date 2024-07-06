package dev.simpleframework.dag.engine;

import java.util.function.Consumer;

/**
 * 有向无环图作业：在执行引擎中执行的节点
 *
 * @author loyayz
 **/
public interface Job {

    /**
     * 获取作业 id
     */
    String id();

    /**
     * 获取作业上下文
     */
    JobContext context();

    /**
     * 获取作业结果
     */
    JobResult result();

    /**
     * 异步获取作业结果：结束作业时回调
     */
    void resultAsync(Consumer<JobResult> handler);

    /**
     * 获取当前运行时快照
     */
    JobSnapshot snapshot();

}
