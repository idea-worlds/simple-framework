package dev.simpleframework.dag.engine.pipeline;

import dev.simpleframework.dag.engine.AbstractJob;

/**
 * 管道作业
 *
 * @author loyayz
 **/
public abstract sealed class PipelineJob
        extends AbstractJob<PipelineRecord>
        permits BaseSourcePipelineJob, BaseSinkPipelineJob, BaseTargetPipelineJob {

    public PipelineJob(String id) {
        super(id);
    }

    protected void emitError(Throwable error) {
        super.emitResult(null, error, null);
    }

    protected void emitComplete() {
        super.emitResult(null, null, null);
    }

}
