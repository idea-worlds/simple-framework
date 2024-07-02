package dev.simpleframework.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程工具类
 *
 * @author loyayz
 **/
public final class Threads {

    /**
     * 创建线程池
     *
     * @param group    线程组名称
     * @param coreSize 核心线程数
     * @param maxSize  最大线程数
     * @apiNote <pre>  keepAliveTime:  10
     * workQueue:      SynchronousQueue
     * rejectedPolicy: CallerRunsPolicy 线程数超出最大值后会在调用线程运行，会阻塞该线程
     * allowCoreThreadTimeOut
     * prestartAllCoreThreads
     * </pre>
     */
    public static ThreadPoolExecutor newPool(String group, int coreSize, int maxSize) {
        if (coreSize < 0) {
            coreSize = 0;
        }
        if (maxSize < coreSize) {
            maxSize = coreSize;
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize, maxSize,
                10L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                newFactory(group),
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    /**
     * 创建线程工厂
     *
     * @param group 线程组名称
     */
    public static ThreadFactory newFactory(String group) {
        return new SimpleThreadFactory(group);
    }

    /**
     * 获取线程池的线程组
     *
     * @param executor 线程池
     */
    public static ThreadGroup getGroup(ExecutorService executor) {
        AtomicReference<ThreadGroup> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Runnable runnable = () -> {
            result.set(Thread.currentThread().getThreadGroup());
            latch.countDown();
        };
        if (executor instanceof ThreadPoolExecutor pool) {
            ThreadFactory factory = pool.getThreadFactory();
            if (factory instanceof SimpleThreadFactory s) {
                return s.group();
            }
            factory.newThread(runnable).start();
        } else {
            executor.submit(runnable);
        }
        try {
            latch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result.get();
    }

    /**
     * 获取线程池中在运行的线程
     */
    public static List<Thread> findActiveThreads(ExecutorService executor) {
        ThreadGroup group = getGroup(executor);
        Thread[] threads = new Thread[group.activeCount()];
        group.enumerate(threads, true);
        return Arrays.asList(threads);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    public static void sleep(Number millis) {
        if (millis == null) {
            return;
        }
        sleep(millis.longValue());
    }

    public static void sleep(Number timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout.longValue());
        } catch (InterruptedException ignore) {
        }
    }

    public static void sleepRandom(int min, int max, TimeUnit timeUnit) {
        int num = ThreadLocalRandom.current().nextInt(min, max);
        sleep(num, timeUnit);
    }

    private static class SimpleThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        SimpleThreadFactory(String groupName) {
            this.group = new ThreadGroup(groupName);
            this.namePrefix = groupName + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }

        ThreadGroup group() {
            return this.group;
        }

    }

}
