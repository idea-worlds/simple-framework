package dev.simpleframework.dag.engine;

import dev.simpleframework.dag.DAG;
import dev.simpleframework.dag.DAGNode;

import java.util.List;

/**
 * 引擎抽象类
 *
 * @param <T> 作业间的传输对象
 * @param <J> 作业实现类
 * @author loyayz
 **/
public abstract class AbstractEngine<T, J extends AbstractJob<T>> implements Engine<J> {
    private final DAG<J> dag;
    private boolean running = false;

    public AbstractEngine() {
        this.dag = new DAG<>();
    }

    protected abstract J createVirtualBeginJob();

    protected abstract J createVirtualFinishJob();

    @Override
    public Engine<J> addJob(J job) {
        DAGNode<J> node = new JobNode<>(job);
        this.dag.addNode(node);
        return this;
    }

    @Override
    public Engine<J> addEdge(String fromKey, String toKey) {
        this.dag.addEdge(fromKey, toKey);
        return this;
    }

    @Override
    public synchronized void exec() {
        if (this.running) {
            return;
        }
        this.dag.visit();
        this.running = true;

        J begin = this.createVirtualBeginJob();
        J finish = this.createVirtualFinishJob();
        this.dag.getStartNode().getValue().subscribe(List.of(begin));
        finish.subscribe(this.dag.getEndNodes().stream().map(DAGNode::getValue).toList());
        begin.emitResult(null, null, null);
    }

    @Override
    public EngineSnapshot snapshot() {
        EngineSnapshot snapshot = new EngineSnapshot();
        this.dag.visit(node -> {
            J job = node.getValue();
            snapshot.jobSnapshot(job.id(), job.snapshot());
        }, false);
        return snapshot;
    }

    static class JobNode<J extends AbstractJob<?>> extends DAGNode<J> {
        JobNode(J job) {
            super(job.id(), job);
            super.setVisitHandler((c, froms, tos) -> {
                List<J> fromJobs = froms.stream().map(DAGNode::getValue).toList();
                job.subscribe(fromJobs);
            });
        }
    }

}
