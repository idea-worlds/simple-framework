package dev.simpleframework.dag.engine.task;

import dev.simpleframework.dag.engine.AbstractEngine;

/**
 * 任务执行引擎：批处理，即只有作业执行完成后，它的后置作业才会执行
 *
 * @author loyayz
 **/
public class TaskEngine extends AbstractEngine<TaskJob> {

    public TaskEngine(String name) {
        super(name);
    }

    @Override
    protected TaskJob createVirtualBeginJob() {
        return new VirtualJob("_begin");
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

