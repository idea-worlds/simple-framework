package dev.simpleframework.dag.engine.pipeline;

import dev.simpleframework.dag.engine.AbstractEngine;

/**
 * 管道执行引擎：流处理，即只要作业中有数据产生，它的后置作业会自动接收
 *
 * @author loyayz
 **/
public class PipelineEngine extends AbstractEngine<PipelineRecord, PipelineJob> {

    public PipelineEngine() {
        super();
    }

    @Override
    protected PipelineJob createVirtualBeginJob() {
        return new VirtualJob("_begin");
    }

    @Override
    protected PipelineJob createVirtualFinishJob() {
        return new VirtualJob("_finish");
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

    }

}
