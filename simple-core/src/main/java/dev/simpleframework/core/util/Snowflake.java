package dev.simpleframework.core.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 雪花算法 id 生成器
 * 参考：<a href="https://gitee.com/yu120/sequence">优化开源项目</a>
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class Snowflake {
    public static final Snowflake DEFAULT = new Snowflake();

    /**
     * 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）
     */
    private final long baseTime;
    /**
     * 数据中心id位数
     */
    private final long datacenterIdBits;
    /**
     * 机器id位数
     */
    private final long workerIdBits;
    /**
     * 每秒内产生的id数
     */
    private final long sequenceBits;

    private final long maxDatacenterId;
    private final long maxWorkerId;
    private final long maxSequence;

    private final long datacenterIdShift;
    private final long workerIdShift;
    /**
     * 时间戳左移动位
     */
    private final long timeShift;
    /**
     * 所属数据中心id
     */
    private final long datacenterId;
    /**
     * 所属机器id
     */
    private final long workerId;
    /**
     * 并发控制序列
     */
    private long sequence = 0L;

    /**
     * 上次生产 ID 时间戳
     */
    private long lastTime = -1L;

    private static volatile InetAddress LOCAL_ADDRESS = null;
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    public Snowflake() {
        this(
                // 默认起始时间（+8 2021-01-01 00:00:00）
                Long.parseLong(System.getProperty("simple.snowflake.time", "1009814400")),
                // 默认每秒可产生id数 2^15 = 32768
                Long.parseLong(System.getProperty("simple.snowflake.seq", "15")),
                // 默认数据中心id位数 2^1 = 2
                Long.parseLong(System.getProperty("simple.snowflake.centerLen", "1")),
                Long.parseLong(System.getProperty("simple.snowflake.centerId", "-1")),
                // 默认数据中心id位数 2^4 = 16
                Long.parseLong(System.getProperty("simple.snowflake.workerLen", "4")),
                Long.parseLong(System.getProperty("simple.snowflake.workerId", "-1"))
        );
    }

    public Snowflake(long baseTime, long sequenceBits, long centerIdBits, long centerId, long workerIdBits, long workerId) {
        this.baseTime = baseTime;
        this.datacenterIdBits = centerIdBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;

        this.maxDatacenterId = ~(-1L << centerIdBits);
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);

        this.workerIdShift = sequenceBits;
        this.datacenterIdShift = this.workerIdShift + workerIdBits;
        this.timeShift = this.datacenterIdShift + centerIdBits;

        this.datacenterId = centerId < 0 ? getDatacenterId() : centerId;
        this.workerId = workerId < 0 ? getWorkerId() : workerId;
    }

    /**
     * 基于网卡MAC地址计算余数作为数据中心
     * <p>
     * 可自定扩展
     */
    protected long getDatacenterId() {
        if (this.datacenterIdBits == 0) {
            return 0;
        }
        long id = 0L;
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(getLocalAddress());
            if (null == network) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    id = ((0x000000FF & (long) mac[mac.length - 2]) | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
                    id = id % (this.maxDatacenterId + 1);
                }
            }
        } catch (Exception ignore) {
        }
        return id;
    }

    /**
     * 基于 MAC + PID 的 hashcode 获取16个低位
     * <p>
     * 可自定扩展
     */
    protected long getWorkerId() {
        StringBuilder mpId = new StringBuilder();
        mpId.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (name != null && name.length() > 0) {
            // GET jvmPid
            mpId.append(name.split("@")[0]);
        }

        // MAC + PID 的 hashcode 获取16个低位
        return (mpId.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    /**
     * 获取下一个 ID
     *
     * @return next id
     */
    public synchronized long nextId() {
        long currentTime = currentTime();
        waitNextTime(currentTime);

        if (lastTime == currentTime) {
            // 相同秒内，序列号自增
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                // 同一秒的序列数已经达到最大
                currentTime = tilNextTime(lastTime);
            }
        } else {
            // 不同毫秒内，序列号置为 1 - 3 随机数
            sequence = ThreadLocalRandom.current().nextLong(1, 3);
        }

        lastTime = currentTime;

        // 时间戳部分 | 机器标识部分 | 序列号部分
        return ((currentTime - baseTime) << timeShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    /**
     * 闰秒
     */
    protected void waitNextTime(long currentTime) {
        if (currentTime < lastTime) {
            long offset = lastTime - currentTime;
            if (offset <= 2) {
                try {
                    wait(1010 * offset);
                    currentTime = currentTime();
                    if (currentTime < lastTime) {
                        throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d seconds", offset));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d seconds", offset));
            }
        }
    }

    protected long tilNextTime(long lastTime) {
        long current = currentTime();
        while (current <= lastTime) {
            current = currentTime();
        }
        return current;
    }

    protected long currentTime() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Find first valid IP from local network card
     *
     * @return first valid local IP
     */
    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }

        LOCAL_ADDRESS = getLocalAddress0();
        return LOCAL_ADDRESS;
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable ignore) {
        }

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            try {
                                InetAddress address = addresses.nextElement();
                                if (isValidAddress(address)) {
                                    return address;
                                }
                            } catch (Throwable ignore) {
                            }
                        }
                    } catch (Throwable ignore) {
                    }
                }
            }
        } catch (Throwable ignore) {
        }
        return localAddress;
    }

    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }

        String name = address.getHostAddress();
        return (name != null && !"0.0.0.0".equals(name) && !"127.0.0.1".equals(name) && IP_PATTERN.matcher(name).matches());
    }

}
