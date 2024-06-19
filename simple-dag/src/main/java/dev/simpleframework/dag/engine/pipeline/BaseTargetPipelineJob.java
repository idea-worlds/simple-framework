package dev.simpleframework.dag.engine.pipeline;

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
    protected abstract void doLoad(PipelineRecord record);

    /**
     * 接收前置作业的数据后执行写入 {@link #doLoad}，写入成功后自动发送至后置作业
     */
    @Override
    protected void onData(String fromJobId, PipelineRecord record) {
        this.doLoad(record);
        this.emitData(record);
    }

    @Override
    protected void onFinish() {
        super.onFinish();
        this.emitComplete();
    }

}
