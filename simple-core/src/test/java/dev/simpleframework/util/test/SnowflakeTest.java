package dev.simpleframework.util.test;

import dev.simpleframework.util.Snowflake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class SnowflakeTest {

    @Test
    public void calcMaxYear() {
        int sequenceBits = 16;
        int centerIdBits = 2;
        int workerIdBits = 5;
        int timeBits = 63 - sequenceBits - centerIdBits - workerIdBits;

        Function<String, String> gen = s -> {
            StringBuilder r = new StringBuilder();
            for (int i = 0; i < timeBits; i++) {
                r.append(s);
            }
            return r.toString();
        };
        long minTime = new BigInteger(gen.apply("0"), 2).longValue();
        long maxTime = new BigInteger(gen.apply("1"), 2).longValue();
        //一年总共多少秒
        long oneYear = 60L * 60 * 24 * 365;
        //算出最大可以多少年
        System.out.println((maxTime - minTime) / oneYear);
    }

    @RepeatedTest(3)
    public void testRepeatedSingle() {
        Set<Long> ids = new HashSet<>();
        int maxTimes = 10000 * 10;

        long begin = System.currentTimeMillis();
        Snowflake snowflake = new Snowflake();
//        Snowflake snowflake = new Snowflake(1609430400L,22,0,0,0,0);
        for (int i = 0; i < maxTimes; i++) {
            ids.add(snowflake.nextId());
        }
        long end = System.currentTimeMillis();
        System.out.println(snowflake.nextId());
        System.out.println(String.valueOf(snowflake.nextId()).length());
        System.out.println(end - begin);
        Assertions.assertEquals(maxTimes, ids.size());
    }

    @RepeatedTest(3)
    public void testRepeatedMultiple() {
        long centerIdBits = ThreadLocalRandom.current().nextLong(0, 5);
        long workerIdBits = ThreadLocalRandom.current().nextLong(0, 5);

        Set<Long> ids = new HashSet<>();
        List<Long> repeatedIds = new ArrayList<>();
        long maxDatacenterId = ~(-1L << centerIdBits);
        long maxWorkerId = ~(-1L << workerIdBits);
        System.out.printf("c:%s/%s, w:%s%s", centerIdBits, maxDatacenterId, workerIdBits, maxWorkerId);

        for (int centerId = 0; centerId <= maxDatacenterId; centerId++) {
            for (int workerId = 0; workerId <= maxWorkerId; workerId++) {
                Snowflake snowflake = new Snowflake(1609430400L, 12L, centerIdBits, centerId, workerIdBits, workerId);
                new SnowflakeThread(ids, repeatedIds, snowflake).start();
            }
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(ids.size());
        Assertions.assertTrue(repeatedIds.isEmpty());
    }

    static class SnowflakeThread extends Thread {
        private final Set<Long> ids;
        private final List<Long> repeatedIds;
        private final Snowflake snowflake;

        public SnowflakeThread(Set<Long> ids, List<Long> repeatedIds, Snowflake snowflake) {
            this.ids = ids;
            this.repeatedIds = repeatedIds;
            this.snowflake = snowflake;
            super.setDaemon(true);
        }

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                long id = snowflake.nextId();
                if (!ids.add(id)) {
                    repeatedIds.add(id);
                    System.err.println("duplicate:" + id);
                }
            }
        }
    }

}
