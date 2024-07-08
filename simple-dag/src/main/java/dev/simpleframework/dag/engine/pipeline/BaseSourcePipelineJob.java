package dev.simpleframework.dag.engine.pipeline;

/**
 * 作业：抽取源数据
 *
 * @author loyayz
 **/
public abstract non-sealed class BaseSourcePipelineJob extends PipelineJob {

    public BaseSourcePipelineJob(String id) {
        super(id);
    }

    /**
     * 抽取
     *
     * @apiNote <pre>  执行完本方法后默认调用 {@link #emitComplete} 结束本次作业
     * 因此，若是异步实现则需阻塞本方法，或重写 {@link #onFinish}
     * 注：需手动调用 {@link #emitData} 发送数据至后置作业</pre>
     */
    protected abstract void doExtract();

    /**
     * 前置作业全成功后才开始执行本作业
     */
    @Override
    protected void onFinishWithAllComplete() {
        try {
            super.start();
            this.doExtract();
            this.emitComplete();
        } catch (Throwable e) {
            this.emitError(e);
        }
    }

}
