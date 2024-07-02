package dev.simpleframework.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 时钟：获取当前系统时间近似值
 *
 * @author loyayz
 **/
public final class Clock {
    private static volatile long now;

    static {
        now = System.currentTimeMillis();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Simple Clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(
                () -> now = System.currentTimeMillis(),
                1, 1, TimeUnit.MILLISECONDS);
    }

    public static long now() {
        return now;
    }

}
