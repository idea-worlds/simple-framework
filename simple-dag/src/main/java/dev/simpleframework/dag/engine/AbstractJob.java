package dev.simpleframework.dag.engine;

import lombok.AccessLevel;
import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 作业抽象类
 *
 * @param <T> 作业间的传输对象
 * @author loyayz
 **/
public abstract class AbstractJob<T> implements Job {
    private final String id;
    private final JobContext context;
    private final JobResult result;
    @Getter(AccessLevel.PROTECTED)
    private Consumer<JobResult> resultHandler;

    @Getter(AccessLevel.PROTECTED)
    private final Flux<T> flux;
    @Getter(AccessLevel.PROTECTED)
    private FluxSink<T> sink;

    public AbstractJob(String id) {
        this.id = id;
        this.context = new JobContext();
        this.result = new JobResult(id);
        this.flux = Flux.<T>create(sink -> this.sink = sink)
                .doFirst(() -> {
                    this.context().initBegin();
                    this.doInit();
                })
                .doFinally(t -> this.doFinally());
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public JobContext context() {
        return this.context;
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
        return new JobSnapshot(this.context, this.result);
    }

    /**
     * 初始化：作业引擎执行时会先调用所有作业的该方法
     */
    protected void doInit() {
    }

    /**
     * 作业结束执行方法
     * <pre>  先执行后置作业的 doFinally 再执行本作业的 doFinally
     * 若想在本作业结束后立即执行，可重写 {@link #emitResult}
     * {@code @Override
     *   protected void emitResult(Object value, Throwable error, Supplier<T> emitDataSupplier) {
     *       try {
     *           this.emitResult(value, error, emitDataSupplier);
     *       } finally {
     *           this.doFinally();
     *       }
     *   }}
     * 注：作业结束后始终会执行本方法，因此如上例时，会执行两次本方法</pre>
     */
    protected void doFinally() {
    }

    /**
     * 监听前置作业发送的数据
     */
    protected void onData(String fromJobId, T record) {
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
     * <pre>  全失败：{@link #onFinishWithAllError}
     * 全成功：{@link #onFinishWithAllComplete}
     * 其他： {@link #onFinishWithAnyComplete}、{@link #onFinishWithAnyError}</pre>
     */
    protected void onFinish() {
        Map<String, Boolean> froms = this.context.froms();
        List<String> errorFroms = new ArrayList<>();
        froms.forEach((from, complete) -> {
            if (!complete) {
                errorFroms.add(from);
            }
        });
        boolean allError = errorFroms.size() == froms.size();
        if (allError) {
            this.onFinishWithAllError();
        } else if (errorFroms.isEmpty()) {
            this.onFinishWithAllComplete();
        } else {
            this.onFinishWithAnyComplete();
            this.onFinishWithAnyError();
        }
    }

    /**
     * 监听前置作业全失败
     */
    protected void onFinishWithAllError() {
    }

    /**
     * 监听前置作业都结束后有任一失败
     */
    protected void onFinishWithAnyError() {
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
    protected void emitData(T data) {
        this.context.incrementEmit();
        this.sink.next(data);
    }

    /**
     * 设置作业结果并发送结束信号至后置作业
     *
     * @param value            结果值
     * @param error            异常值，有异常时发送异常结束信号，无异常时发送正常结束信号
     * @param emitDataSupplier 发送结束信号前，若执行此回调有数据，则先发送该数据
     */
    protected void emitResult(Object value, Throwable error, Supplier<T> emitDataSupplier) {
        this.setFinishData(value, error);
        T data = emitDataSupplier == null ? null : emitDataSupplier.get();
        if (data != null) {
            this.emitData(data);
        }
        if (error == null) {
            this.sink.complete();
        } else {
            this.sink.error(error);
        }
        if (this.resultHandler != null) {
            this.resultHandler.accept(this.result);
        }
    }

    private void setFinishData(Object value, Throwable error) {
        this.context.setFinish();
        this.result.setSuccess(error == null);
        this.result.setValue(value);
        this.result.setError(error);
        this.result.fill(this.context);
    }

    public void subscribe(List<? extends Job> fromJobs) {
        this.context.initFroms(fromJobs.stream().map(Job::id).collect(Collectors.toList()));
        for (Job job : fromJobs) {
            AbstractJob<T> fromJob = (AbstractJob<T>) job;
            String fromJobId = fromJob.id();
            fromJob.flux.subscribe(
                    data -> {
                        this.context.incrementReceive();
                        this.onData(fromJobId, data);
                    },
                    error -> {
                        this.onError(fromJobId, error);
                        if (this.context.removeRunning(fromJobId, false)) {
                            this.onFinish();
                        }
                    },
                    () -> {
                        this.onComplete(fromJobId);
                        if (this.context.removeRunning(fromJobId, true)) {
                            this.onFinish();
                        }
                    }
            );
        }
    }

}
