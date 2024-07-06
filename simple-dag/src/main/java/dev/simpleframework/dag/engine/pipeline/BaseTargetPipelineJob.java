package dev.simpleframework.dag.engine.pipeline;

import dev.simpleframework.dag.engine.JobRecord;

/**
 * 作业：写入目标数据
 *
 * @author loyayz
 **/
public abstract non-sealed class BaseTargetPipelineJob extends PipelineJob {

    public BaseTargetPipelineJob(String id) {
        super(id);
    }

    /**
     * 写入
     */
    protected abstract void doLoad(JobRecord record);

    /**
     * 接收前置作业的数据后执行写入 {@link #doLoad}，写入成功后自动发送至后置作业
     */
    @Override
    protected void onData(JobRecord data) {
        this.doLoad(data);
        this.emitData(data.copy(super.id()));
    }

    @Override
    protected void onFinish() {
        super.onFinish();
        this.emitComplete();
    }

}
