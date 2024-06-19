package dev.simpleframework.dag.engine.task;

import dev.simpleframework.dag.engine.AbstractEngine;
import dev.simpleframework.dag.engine.JobResult;

/**
 * 任务执行引擎：批处理，即只有作业执行完成后，它的后置作业才会执行
 *
 * @author loyayz
 **/
public class TaskEngine extends AbstractEngine<JobResult, TaskJob> {

    public TaskEngine() {
        super();
    }

    @Override
    protected TaskJob createVirtualBeginJob() {
        return new VirtualJob("_begin");
    }

    @Override
    protected TaskJob createVirtualFinishJob() {
        return new VirtualJob("_finish");
    }

    static class VirtualJob extends TaskJob {
        public VirtualJob(String id) {
            super(id);
        }

        @Override
        protected void onFinish() {
        }
    }

}

