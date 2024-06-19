package dev.simpleframework.dag.engine.task;

import dev.simpleframework.dag.engine.AbstractJob;
import dev.simpleframework.dag.engine.JobResult;

/**
 * 任务作业
 *
 * @author loyayz
 **/
public class TaskJob extends AbstractJob<JobResult> {

    public TaskJob(String id) {
        super(id);
    }

    protected void emitError(Throwable error) {
        super.emitResult(null, error, super::result);
    }

    protected void emitComplete(Object result) {
        super.emitResult(result, null, super::result);
    }

}
