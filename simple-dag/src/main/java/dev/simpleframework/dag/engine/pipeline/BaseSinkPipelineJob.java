package dev.simpleframework.dag.engine.pipeline;

import dev.simpleframework.dag.engine.JobRecord;

/**
 * 作业：转换数据
 *
 * @author loyayz
 **/
public abstract non-sealed class BaseSinkPipelineJob extends PipelineJob {

    public BaseSinkPipelineJob(String id) {
        super(id);
    }

    /**
     * 转换，不为 null 的数据自动发送至后置作业
     */
    protected abstract JobRecord doTransform(JobRecord record);

    /**
     * 接收前置作业的数据后执行转换 {@link #doTransform}，不为 null 的数据自动发送至后置作业
     */
    @Override
    protected void onData(JobRecord data) {
        data = this.doTransform(data);
        if (data != null) {
            this.emitData(data.copy(super.id()));
        }
    }

    @Override
    protected void onFinish() {
        super.onFinish();
        this.emitComplete();
    }

}
