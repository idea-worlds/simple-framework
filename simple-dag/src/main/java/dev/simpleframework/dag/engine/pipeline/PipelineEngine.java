package dev.simpleframework.dag.engine.pipeline;

import dev.simpleframework.dag.engine.AbstractEngine;
import dev.simpleframework.dag.engine.JobRecord;

/**
 * 管道执行引擎：流处理，即只要作业中有数据产生，它的后置作业会自动接收
 *
 * @author loyayz
 **/
public class PipelineEngine extends AbstractEngine<PipelineJob> {

    public PipelineEngine(String name) {
        super(name);
    }

    @Override
    protected PipelineJob createVirtualBeginJob() {
        return new VirtualJob("_begin");
    }

    static class VirtualJob extends BaseSourcePipelineJob {
        public VirtualJob(String id) {
            super(id);
        }

        @Override
        protected void doExtract() {
        }

        @Override
        protected void onFinish() {
        }

        @Override
        protected void emitData(JobRecord data) {
        }

    }

}
