package dev.simpleframework.dag.engine.pipeline;

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
     * 转换
     */
    protected abstract PipelineRecord doTransform(PipelineRecord record);

    /**
     * 接收前置作业的数据后执行转换 {@link #doTransform}，不为 null 的数据自动发送至后置作业
     */
    @Override
    protected void onData(String fromJobId, PipelineRecord record) {
        record = this.doTransform(record);
        if (record != null) {
            this.emitData(record);
        }
    }

    @Override
    protected void onFinish() {
        super.onFinish();
        this.emitComplete();
    }

}
