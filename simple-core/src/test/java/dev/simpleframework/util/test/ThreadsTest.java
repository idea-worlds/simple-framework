package dev.simpleframework.util.test;

import dev.simpleframework.util.Threads;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author loyayz
 **/
public class ThreadsTest {

    @Test
    public void getGroup() {
        String groupName = UUID.randomUUID().toString();
        ThreadPoolExecutor pool = Threads.newPool(groupName, 1, 2);
        assertGroup(pool, groupName);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        assertGroup(executorService, Thread.currentThread().getThreadGroup().getName());
    }

    @Test
    public void listActiveThreads() {
        ThreadPoolExecutor pool;
        for (int max = 1; max <= 5; max++) {
            for (int core = max; core <= max; core++) {
                for (int num = 1; num <= max + 1; num++) {
                    pool = Threads.newPool(UUID.randomUUID().toString(), core, max);
                    System.out.println("core: " + core + ", max: " + max + ", num: " + num);
                    assertActive(pool, num);
                    pool.shutdown();
                }
            }
        }
    }

    private static void assertGroup(ExecutorService pool, String groupName) {
        pool.submit(() -> {
            ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
            Assertions.assertEquals(Threads.getGroup(pool), currentGroup);
            Assertions.assertEquals(groupName, currentGroup.getName());
        });
    }

    @SneakyThrows
    private static void assertActive(ThreadPoolExecutor pool, int threadNum) {
        AtomicInteger count = new AtomicInteger(100);
        CountDownLatch running = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            pool.submit(() -> {
                running.countDown();
                while (count.decrementAndGet() > 0) {
                    Threads.sleep(10);
                }
            });
        }
        running.await();

        int expectNum = Math.min(threadNum, pool.getMaximumPoolSize());
        while (count.get() > 0) {
            List<Thread> threads = Threads.findActiveThreads(pool);
            Assertions.assertEquals(expectNum, threads.size());
            Threads.sleep(10);
        }
    }

}
