package dev.simpleframework.dag.engine.pipeline;

import dev.simpleframework.dag.engine.AbstractJob;
import dev.simpleframework.dag.engine.JobRecord;

/**
 * 管道作业
 *
 * @author loyayz
 **/
public abstract sealed class PipelineJob
        extends AbstractJob
        permits BaseSourcePipelineJob, BaseSinkPipelineJob, BaseTargetPipelineJob {

    public PipelineJob(String id) {
        super(id);
    }

}
