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

    /**
     * 前置作业全成功，说明本作业已经处理完所有接收到的数据，此时应结束本作业
     */
    @Override
    protected void onFinishWithAllComplete() {
        this.emitComplete();
    }

}
