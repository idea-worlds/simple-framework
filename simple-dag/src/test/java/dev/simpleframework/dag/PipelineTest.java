package dev.simpleframework.dag;

import dev.simpleframework.dag.engine.EngineResult;
import dev.simpleframework.dag.engine.JobRecord;
import dev.simpleframework.dag.engine.JobResult;
import dev.simpleframework.dag.engine.pipeline.BaseSourcePipelineJob;
import dev.simpleframework.dag.engine.pipeline.BaseTargetPipelineJob;
import dev.simpleframework.dag.engine.pipeline.PipelineEngine;
import dev.simpleframework.util.Strings;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author loyayz
 **/
public class PipelineTest {

    /**
     * * 1 →→→→ 2
     */
    @Test
    public void one_one() {
        PipelineEngine engine = new PipelineEngine("one_one");
        engine.addJob(new MockSource("1"))
                .addJob(new ConsolePipelineJob("2"));
        Map<String, List<String>> edges = new HashMap<>();
        edges.put("1", List.of("2"));
        exec(engine, edges);
    }

    /**
     * * 1 →→→→ 2 →→→→ 3
     */
    @Test
    public void one_one_one() {
        PipelineEngine engine = new PipelineEngine("one_one");
        engine.addJob(new MockSource("1"))
                .addJob(new ConsolePipelineJob("2"))
                .addJob(new ConsolePipelineJob("3"));
        Map<String, List<String>> edges = new HashMap<>();
        edges.put("1", List.of("2"));
        edges.put("2", List.of("3"));
        exec(engine, edges);
    }

    /**
     * * 1 →→→→ 2
     * * ↓
     * * ↓
     * * 3
     */
    @Test
    public void one_two() {
        PipelineEngine engine = new PipelineEngine("one_two");
        engine.addJob(new MockSource("1"))
                .addJob(new ConsolePipelineJob("2"))
                .addJob(new ConsolePipelineJob("3"));
        Map<String, List<String>> edges = new HashMap<>();
        edges.put("1", List.of("2", "3"));
        exec(engine, edges);
    }

    /**
     * * 1 →→→→ 2
     * * ↓      ↓
     * * ↓      ↓
     * * 3 →→→→ 4
     */
    @Test
    public void one_two_one() {
        PipelineEngine engine = new PipelineEngine("one_two");
        engine.addJob(new MockSource("1"))
                .addJob(new ConsolePipelineJob("2"))
                .addJob(new ConsolePipelineJob("3"))
                .addJob(new ConsolePipelineJob("4"));
        Map<String, List<String>> edges = new HashMap<>();
        edges.put("1", List.of("2", "3"));
        edges.put("2", List.of("4"));
        edges.put("3", List.of("4"));
        exec(engine, edges);
    }

    /**
     * * 1 →→→→ 3 →→→→          →→→→ 9
     * * ↓      ↑      ↓      ↑
     * * ↓      ↑      ↓      ↑
     * *   →→→→ 4 →→→→ 2 →→→→ 5 →→→→ 6
     * *                      ↓      ↑
     * *                      ↓      ↑
     * *                      7 →→→→ 8
     */
    @Test
    public void complex() {
        PipelineEngine engine = new PipelineEngine("complex");
        engine.addJob(new MockSource("1"))
                .addJob(new ConsolePipelineJob("2"))
                .addJob(new ConsolePipelineJob("3"))
                .addJob(new ConsolePipelineJob("4"))
                .addJob(new ConsolePipelineJob("5"))
                .addJob(new ConsolePipelineJob("6"))
                .addJob(new ConsolePipelineJob("7"))
                .addJob(new ConsolePipelineJob("8"))
                .addJob(new ConsolePipelineJob("9"));
        Map<String, List<String>> edges = new HashMap<>();
        edges.put("1", List.of("3", "4"));
        edges.put("3", List.of("2"));
        edges.put("4", List.of("2", "3"));
        edges.put("2", List.of("5"));
        edges.put("5", List.of("6", "7", "9"));
        edges.put("7", List.of("8"));
        edges.put("8", List.of("6"));
        exec(engine, edges);
    }

    static void exec(PipelineEngine engine, Map<String, List<String>> edges) {
        edges.forEach(engine::addEdge);

        EngineResult result = engine.exec();
        Assertions.assertTrue(result.getStatus().isFinish());
        System.out.println("\n" + result);
        System.out.println(Strings.readableTime(result.getRunTime()));
        assertReceive(result);

        Map<String, JobResult> jobResults = result.getJobs();
        edges.forEach((from, tos) -> Assertions.assertTrue(jobResults.containsKey(from)));
        for (Map.Entry<String, JobResult> entry : jobResults.entrySet()) {
            String jobId = entry.getKey();
            JobResult jobResult = entry.getValue();
            List<String> exceptedFroms = new ArrayList<>();
            edges.forEach((from, tos) -> {
                if (tos.stream().anyMatch(to -> to.equals(jobId))) {
                    exceptedFroms.add(from);
                }
            });
            if (exceptedFroms.isEmpty()) {
                continue;
            }
            Map<String, Object> resultValue = jobResult.value();
            Set<String> actualFroms = (Set<String>) resultValue.get("froms");
            Assertions.assertTrue(actualFroms.size() == exceptedFroms.size()
                    && actualFroms.containsAll(exceptedFroms));
        }
    }

    static void assertReceive(EngineResult result) {
        Map<String, JobResult> jobResults = result.getJobs();
        for (Map.Entry<String, JobResult> entry : jobResults.entrySet()) {
            JobResult jobResult = entry.getValue();
            Map<String, Object> resultValue = jobResult.value();
            Set<String> actualFroms = (Set<String>) resultValue.get("froms");
            if (actualFroms == null) {
                continue;
            }

            long actualNum = jobResult.getCountReceive();
            int expectedNum = 0;
            for (String from : actualFroms) {
                JobResult fromResult = jobResults.get(from);
                expectedNum += fromResult.getCountEmit();
            }
            Assertions.assertEquals(expectedNum, actualNum);
        }
    }

    static class MockSource extends BaseSourcePipelineJob {
        private final Set<String> threads;

        public MockSource(String id) {
            super(id);
            this.threads = Collections.synchronizedSet(new HashSet<>());
        }

        @Override
        @SneakyThrows
        protected void doExtract() {
            int max = 1000000;
            for (int i = 1; i <= max; i++) {
                String thread = Thread.currentThread().getName();
                threads.add(thread);

                Map<String, Object> value = new HashMap<>();
                value.put(i + "", thread);

                JobRecord data = super.buildRecord(value);
                if (i % 1000000 == 0) {
                    System.out.println(this.id() + " emitData  --> " + data + " on " + thread);
                }
                super.emitData(data);
            }
        }

        @Override
        protected void doInit() {
            System.out.println(this.id() + " doInit    on " +
                    Thread.currentThread().getName());
            super.doInit();
        }

        @Override
        protected void doFinally() {
            System.out.println(this.id() + " doFinally on " +
                    Thread.currentThread().getName());
            super.doFinally();
        }

        @Override
        protected void onFinish() {
            System.out.println(this.id() + " onFinish  on " +
                    Thread.currentThread().getName());
            super.onFinish();
        }

        @Override
        protected void emitResult(Throwable error) {
            System.out.println(this.id() + " emitResult on " +
                    Thread.currentThread().getName());
            super.emitResult(error);
        }

        @Override
        protected Object buildResultValue() {
            Map<String, Object> result = new HashMap<>();
            result.put("threads", threads);
            return result;
        }

    }

    static class ConsolePipelineJob extends BaseTargetPipelineJob {
        private final Set<String> dataFromJobs;
        private final Set<String> threads;

        public ConsolePipelineJob(String id) {
            super(id);
            this.dataFromJobs = Collections.synchronizedSet(new HashSet<>());
            this.threads = Collections.synchronizedSet(new HashSet<>());
        }

        @Override
        protected void doLoad(JobRecord data) {
            String thread = Thread.currentThread().getName();
            this.dataFromJobs.add(data.getFrom());
            this.threads.add(thread);

            long countReceive = super.context().countReceive();
            if (countReceive % 1000000 == 0) {
                System.out.println(this.id() + " doLoad    --> " + countReceive + " on " + threads);
            }
        }

        @Override
        protected void doInit() {
            System.out.println(this.id() + " doInit    on " +
                    Thread.currentThread().getName());
            super.doInit();
        }

        @Override
        protected void doFinally() {
            System.out.println(this.id() + " doFinally on " +
                    Thread.currentThread().getName());
            super.doFinally();
        }

        @Override
        protected void onFinish() {
            System.out.println(this.id() + " onFinish  on " +
                    Thread.currentThread().getName());
            super.onFinish();
        }

        @Override
        protected void emitResult(Throwable error) {
            System.out.println(this.id() + " emitResult on" +
                    Thread.currentThread().getName());
            super.emitResult(error);
        }

        @Override
        protected Object buildResultValue() {
            Map<String, Object> result = new HashMap<>();
            result.put("froms", dataFromJobs);
            result.put("threads", threads);
            return result;
        }

    }

}
