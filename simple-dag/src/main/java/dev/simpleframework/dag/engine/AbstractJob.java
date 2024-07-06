package dev.simpleframework.dag.engine;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
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
    @Getter(AccessLevel.PROTECTED)
    private Consumer<JobResult> resultHandler;
    private JobEventProducer target;

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
    public void abort() {
        if (this.context().status().isFinish()) {
            return;
        }
        try {
            this.doAbort();
        } finally {
            this.context().abort();
            if (this.target != null) {
                this.target.shutdown();
            }
        }
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
     * 作业中止时执行方法
     */
    protected void doAbort() {
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
     * 监听前置作业中止
     */
    protected void onAbort(String fromJobId) {
    }

    /**
     * 监听前置作业异常结束
     */
    protected void onError(String fromJobId, Throwable e) {
    }

    /**
     * 监听前置作业正常结束
     */
    protected void onComplete(String fromJobId) {
    }

    /**
     * 监听前置作业都结束
     *
     * @apiNote <pre>  全部失败 {@link #onFinishWithAllFail}
     * 全部成功 {@link #onFinishWithAllComplete}
     * 任一失败 {@link #onFinishWithAnyComplete}
     * 任一成功 {@link #onFinishWithAnyFail}
     */
    protected void onFinish() {
        Map<String, RunStatus> froms = this.jobContext.froms();
        int total = froms.size();
        int complete = 0;
        int fail = 0;
        for (RunStatus status : froms.values()) {
            if (status == RunStatus.COMPLETE) {
                complete++;
            } else if (status == RunStatus.FAIL) {
                fail++;
            }
        }
        if (complete == total) {
            this.onFinishWithAllComplete();
        } else if (fail == total) {
            this.onFinishWithAllFail();
        } else {
            if (complete > 0) {
                this.onFinishWithAnyComplete();
            } else if (fail > 0) {
                this.onFinishWithAnyFail();
            }
        }
    }

    /**
     * 监听前置作业全失败
     */
    protected void onFinishWithAllFail() {
    }

    /**
     * 监听前置作业都结束后有任一失败
     */
    protected void onFinishWithAnyFail() {
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
        if (this.target != null) {
            this.target.emitData(record);
        }
    }

    protected void emitComplete() {
        this.emitResult(null);
    }

    protected void emitError(Throwable error) {
        this.emitResult(error);
    }

    /**
     * 设置作业结果并发送结束信号至后置作业
     *
     * @param error 异常值，有异常时发送异常结束信号，无异常时发送正常结束信号
     */
    protected void emitResult(Throwable error) {
        try {
            if (error == null) {
                Object value = this.buildResultValue();
                this.result.setValue(value);
            }
            this.jobContext.setFinish(error);
            this.result.fill(this.jobContext);
            this.result.setError(error);

            if (this.target != null) {
                if (error == null) {
                    this.target.emitComplete();
                } else {
                    this.target.emitError(error);
                }
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

    protected JobRecord buildRecord(Object value) {
        return new JobRecord(this.id(), value);
    }

    void engineContext(EngineContext engineContext) {
        this.engineContext = engineContext;
    }

    void listen(AbstractJob from) {
        from.createProducer();
        from.subscribe(event -> {
            String fromJobId = event.getJobId();
            RunStatus status = event.getStatus();
            Throwable error = event.getError();
            JobRecord record = event.getRecord();
            if (status == RunStatus.RUNNING) {
                this.jobContext.incrementReceive();
                this.onData(record);
            } else if (status == RunStatus.COMPLETE) {
                this.onComplete(fromJobId);
                if (this.jobContext.decrementRunning(fromJobId, status)) {
                    this.onFinish();
                }
            } else if (status == RunStatus.FAIL) {
                this.onError(fromJobId, error);
                if (this.jobContext.decrementRunning(fromJobId, status)) {
                    this.onFinish();
                }
            }
        });
    }

    void subscribe(List<? extends AbstractJob> fromJobs, List<? extends AbstractJob> toJobs) {
        fromJobs.forEach(this::listen);
        if (!toJobs.isEmpty()) {
            this.createProducer();
        }
        List<String> fromJobIds = fromJobs.stream().map(Job::id).toList();
        this.jobContext.initBegin(fromJobIds);
        this.result.fill(this.jobContext);
    }

    private void createProducer() {
        if (this.target != null) {
            return;
        }
        int bufferSize = 1024;
        ThreadFactory threadFactory = this.engineContext.threadFactory();
        ProducerType producerType = ProducerType.MULTI;
        BlockingWaitStrategy waitStrategy = new BlockingWaitStrategy();
        Disruptor<JobEvent> disruptor = new Disruptor<>(
                EVENT_FACTORY, bufferSize, threadFactory, producerType, waitStrategy);
        this.target = new JobEventProducer(this.id(), disruptor);
    }

    private void subscribe(Consumer<JobEvent> subscriber) {
        this.target.disruptor.handleEventsWith(
                (event, sequence, endOfBatch) -> subscriber.accept(event));
    }

    void start() {
        if (this.target != null) {
            this.target.start();
        }
    }

    @Data
    static class JobEvent {
        private String jobId;
        private RunStatus status;
        private JobRecord record;
        private Throwable error;
    }

    static class JobEventProducer {
        final String jobId;
        final Disruptor<JobEvent> disruptor;
        final RingBuffer<JobEvent> ringBuffer;

        JobEventProducer(String jobId, Disruptor<JobEvent> disruptor) {
            this.jobId = jobId;
            this.disruptor = disruptor;
            this.ringBuffer = disruptor.getRingBuffer();
        }

        void emitEvent(JobEvent source) {
            long sequence = this.ringBuffer.next();
            try {
                JobEvent event = this.ringBuffer.get(sequence);
                event.setJobId(source.jobId);
                event.setStatus(source.status);
                event.setRecord(source.record);
                event.setError(source.error);
            } finally {
                this.ringBuffer.publish(sequence);
            }
        }

        void emitData(JobRecord record) {
            long sequence = this.ringBuffer.next();
            try {
                JobEvent event = this.ringBuffer.get(sequence);
                event.setJobId(this.jobId);
                event.setStatus(RunStatus.RUNNING);
                event.setRecord(record);
            } finally {
                this.ringBuffer.publish(sequence);
            }
        }

        void emitError(Throwable error) {
            long sequence = this.ringBuffer.next();
            try {
                JobEvent event = this.ringBuffer.get(sequence);
                event.setJobId(this.jobId);
                event.setStatus(RunStatus.FAIL);
                event.setError(error);
            } finally {
                this.ringBuffer.publish(sequence);
            }
        }

        void emitComplete() {
            long sequence = this.ringBuffer.next();
            try {
                JobEvent event = this.ringBuffer.get(sequence);
                event.setJobId(this.jobId);
                event.setStatus(RunStatus.COMPLETE);
            } finally {
                this.ringBuffer.publish(sequence);
            }
        }

        void start() {
            if (this.disruptor.hasStarted()) {
                return;
            }
            this.disruptor.start();
        }

        void shutdown() {
            if (this.disruptor.hasStarted()) {
                this.disruptor.shutdown();
            }
        }

    }

    static EventFactory<JobEvent> EVENT_FACTORY = JobEvent::new;

}
