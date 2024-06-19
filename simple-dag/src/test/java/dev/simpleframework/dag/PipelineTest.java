package dev.simpleframework.dag;

import dev.simpleframework.dag.engine.Engine;
import dev.simpleframework.dag.engine.pipeline.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author loyayz
 **/
public class PipelineTest {

    @Test
    public void test() {
        Engine<PipelineJob> engine = new PipelineEngine()
                .addJob(new MockSource("1"))
                .addJob(new ConsolePipelineJob("2"))
                .addEdge("1", "2");
        engine.exec();

        try {
            Thread.sleep(20000);
        } catch (Exception ignore) {
        }
    }

    static class MockSource extends BaseSourcePipelineJob {
        private static final AtomicInteger counter = new AtomicInteger(0);

        public MockSource(String id) {
            super(id);
        }

        @Override
        @SneakyThrows
        protected void doExtract() {
            int max = 20;
            Consumer<String> action = s -> {
                String thread = Thread.currentThread().getName();
                int i = counter.getAndIncrement();
                while (i <= max) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(i + "", thread);
                    super.emitData(new PipelineRecord(data));

                    i = counter.getAndIncrement();
                }
            };

            Thread[] threads = new Thread[5];
            for (int i = 0; i < 5; i++) {
                threads[i] = new Thread(() -> action.accept(""));
            }
            for (Thread thread : threads) {
                thread.start();
            }
            while (counter.get() <= max) {
                try {
                    Thread.sleep(200);
                } catch (Exception ignore) {
                }
            }
        }

    }

    static class ConsolePipelineJob extends BaseTargetPipelineJob {

        public ConsolePipelineJob(String id) {
            super(id);
        }

        @Override
        protected void doLoad(PipelineRecord record) {
            try {
                long ms = ThreadLocalRandom.current().nextLong(5, 20) * 10;
                Thread.sleep(ms);
            } catch (Exception ignore) {
            }
            for (Map.Entry<String, Object> entry : record.getData().entrySet()) {
                System.out.println(super.id() + " doLoad: " + entry.getKey() + " -> " + entry.getValue() + " " + System.currentTimeMillis());
            }
        }

    }

}
