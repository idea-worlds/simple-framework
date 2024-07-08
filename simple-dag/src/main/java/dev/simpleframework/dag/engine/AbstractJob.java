package dev.simpleframework.dag.engine;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 作业抽象类
 *
 * @author loyayz
 **/
public abstract class AbstractJob implements Job {
    private EngineContext engineContext;
    private final JobContext jobContext;
    private final JobResult result;
    private Consumer<JobResult> resultHandler;
    private JobEventProducer eventProducer;

    public AbstractJob(String id) {
        this.jobContext = new JobContext(id);
        this.result = new JobResult(id);
    }

    @Override
    public String id() {
        return this.jobContext.id();
    }

    @Override
    public JobContext context() {
        return this.jobContext;
    }

    @Override
    public JobResult result() {
        return this.result;
    }

    @Override
    public void resultAsync(Consumer<JobResult> handler) {
        this.resultHandler = handler;
    }

    @Override
    public JobSnapshot snapshot() {
        return new JobSnapshot(this.jobContext, this.result);
    }

    /**
     * 初始化：作业引擎执行时会先调用所有作业的该方法
     */
    protected void doInit() {
    }

    /**
     * 作业结束执行方法
     */
    protected void doFinally() {
    }

    /**
     * 监听前置作业发送的数据
     */
    protected void onData(JobRecord record) {
    }

    /**
     * 监听前置作业异常结束
     */
    protected void onError(String fromJobId, Throwable e) {
    }

    /**
     * 监听前置作业正常结束
     */
    protected void onComplete(String fromJobId, Object result) {
    }

    /**
     * 监听前置作业都结束
     *
     * @apiNote <pre>  全部失败 {@link #onFinishWithAllFail}：  默认取消本作业
     * 全部成功 {@link #onFinishWithAllComplete}
     * 任一成功 {@link #onFinishWithAnyComplete}
     * 任一失败 {@link #onFinishWithAnyFail}：  默认取消本作业
     * 任一取消 {@link #onFinishWithAnyCancel}：默认取消本作业
     */
    protected void onFinish() {
        Map<String, RunStatus> froms = this.jobContext.froms();
        int total = froms.size();
        int complete = 0;
        int fail = 0;
        int cancel = 0;
        for (RunStatus status : froms.values()) {
            if (status == RunStatus.COMPLETE) {
                complete++;
            } else if (status == RunStatus.FAIL) {
                fail++;
            } else if (status == RunStatus.CANCEL) {
                cancel++;
            }
        }
        if (complete == total) {
            this.onFinishWithAllComplete();
        } else if (fail == total) {
            this.onFinishWithAllFail();
        } else {
            if (complete > 0) {
                this.onFinishWithAnyComplete();
            }
            if (fail > 0) {
                this.onFinishWithAnyFail();
            }
            if (cancel > 0) {
                this.onFinishWithAnyCancel();
            }
        }
    }

    /**
     * 监听前置作业全失败
     */
    protected void onFinishWithAllFail() {
        this.emitCancel();
    }

    /**
     * 监听前置作业都结束后有任一失败
     */
    protected void onFinishWithAnyFail() {
        this.emitCancel();
    }

    /**
     * 监听前置作业都结束后有任一取消
     */
    protected void onFinishWithAnyCancel() {
        this.emitCancel();
    }

    /**
     * 监听前置作业全成功
     */
    protected void onFinishWithAllComplete() {
    }

    /**
     * 监听前置作业都结束后有任一成功
     */
    protected void onFinishWithAnyComplete() {
    }

    /**
     * 发送数据至后置作业
     */
    protected void emitData(JobRecord record) {
        this.jobContext.incrementEmit();
        if (this.eventProducer != null) {
            this.eventProducer.emit(RunStatus.RUNNING, record);
        }
    }

    protected void emitComplete() {
        this.emitResult(RunStatus.COMPLETE, null);
    }

    protected void emitError(Throwable error) {
        this.emitResult(RunStatus.FAIL, error);
    }

    protected void emitCancel() {
        this.emitResult(RunStatus.CANCEL, null);
    }

    /**
     * 设置作业结果并发送结束信号至后置作业
     *
     * @param error 异常值，有异常时发送异常结束信号，无异常时发送正常结束信号
     */
    protected void emitResult(RunStatus status, Throwable error) {
        if (this.jobContext.status().isFinish()) {
            return;
        }
        try {
            Object resultValue = null;
            if (status == RunStatus.COMPLETE) {
                resultValue = this.buildResultValue();
                this.result.setValue(resultValue);
            }
            this.jobContext.setStatus(status);
            this.result.fill(this.jobContext);
            this.result.setError(error);

            if (this.eventProducer != null) {
                Object emitValue = null;
                if (status == RunStatus.COMPLETE) {
                    emitValue = resultValue;
                } else if (status == RunStatus.FAIL) {
                    emitValue = error;
                }
                this.eventProducer.emit(status, emitValue);
            }
            if (this.resultHandler != null) {
                this.resultHandler.accept(this.result);
            }
        } finally {
            this.doFinally();
        }
    }

    /**
     * 设置作业结果
     */
    protected Object buildResultValue() {
        return null;
    }

    /**
     * 构建上下文数据对象
     */
    protected JobRecord buildRecord(Object value) {
        return new JobRecord(this.id(), value);
    }

    /**
     * Disruptor 事件缓冲池大小
     */
    protected int eventProducerBufferSize() {
        int defaultMinSize = 512;
        int defaultMaxSize = 1024 * 8;
        int mockEventMemory = 1024;
        double freeMemory = Runtime.getRuntime().freeMemory() * 0.8;
        int size = (int) freeMemory / mockEventMemory;
        if (size > defaultMaxSize) {
            size = defaultMaxSize;
        } else if (size < defaultMinSize) {
            size = defaultMinSize;
        }
        return size;
    }

    /**
     * Disruptor 事件等待策略
     */
    protected WaitStrategy eventProducerWaitStrategy() {
        return new BlockingWaitStrategy();
    }

    void engineContext(EngineContext engineContext) {
        this.engineContext = engineContext;
    }

    void listen(List<? extends AbstractJob> fromJobs) {
        fromJobs.forEach(this::listen);
        List<String> fromJobIds = fromJobs.stream().map(Job::id).toList();
        this.jobContext.setFroms(fromJobIds);
        this.result.fill(this.jobContext);
    }

    void listen(AbstractJob from) {
        from.eventProducer().subscribe(event -> {
            String fromJobId = event.getJobId();
            RunStatus status = event.getStatus();
            Object value = event.getValue();
            if (status == RunStatus.RUNNING) {
                this.jobContext.incrementReceive(fromJobId);
                this.onData((JobRecord) value);
            } else if (status == RunStatus.COMPLETE) {
                this.onComplete(fromJobId, value);
            } else if (status == RunStatus.FAIL) {
                this.onError(fromJobId, (Throwable) value);
            }
            if (status.isFinish()) {
                if (this.jobContext.decrementRunning(fromJobId, status)) {
                    this.onFinish();
                }
            }
        });
    }

    private JobEventProducer eventProducer() {
        if (this.eventProducer == null) {
            Disruptor<JobEvent> disruptor = new Disruptor<>(
                    EVENT_FACTORY,
                    this.eventProducerBufferSize(),
                    this.engineContext.threadFactory(),
                    ProducerType.MULTI,
                    this.eventProducerWaitStrategy());
            this.eventProducer = new JobEventProducer(this.id(), disruptor);
        }
        return this.eventProducer;
    }

    protected void start() {
        if (this.eventProducer != null) {
            this.eventProducer.start();
        }
        this.jobContext.setStatus(RunStatus.RUNNING);
        this.result.fill(this.jobContext);
    }

    void clear() {
        if (this.eventProducer != null) {
            this.eventProducer.shutdown();
        }
    }

    void abort() {
        this.emitResult(RunStatus.ABORT, null);
    }

    static EventFactory<JobEvent> EVENT_FACTORY = JobEvent::new;

    @Data
    static class JobEvent {
        private String jobId;
        private RunStatus status;
        private Object value;
    }

    static class JobEventProducer {
        final String jobId;
        final Disruptor<JobEvent> disruptor;
        final RingBuffer<JobEvent> ringBuffer;

        static EventTranslatorThreeArg<JobEvent, String, RunStatus, Object>
                TRANSLATOR = (event, sequence, jobId, status, value) -> {
            event.setJobId(jobId);
            event.setStatus(status);
            event.setValue(value);
        };

        JobEventProducer(String jobId, Disruptor<JobEvent> disruptor) {
            this.jobId = jobId;
            this.disruptor = disruptor;
            this.ringBuffer = disruptor.getRingBuffer();
        }

        void emit(RunStatus status, Object value) {
            this.ringBuffer.publishEvent(TRANSLATOR, this.jobId, status, value);
        }

        void start() {
            if (!this.disruptor.hasStarted()) {
                this.disruptor.start();
            }
        }

        void shutdown() {
            if (this.disruptor.hasStarted()) {
                this.disruptor.shutdown();
            }
        }

        void subscribe(Consumer<JobEvent> subscriber) {
            this.disruptor.handleEventsWith(
                    (event, sequence, endOfBatch) -> subscriber.accept(event));
        }

    }

}
