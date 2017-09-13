package com.sender.provider.service;

import org.springframework.stereotype.Service;

/**
 * twitter的snowflake算法 -- java实现
 */
@Service
public class SnowFlakeService {

    // 时间flag 2017-01-01 00:00:00 时刻的毫秒数
    private static final long START_TIME_STAMP = 1483200000000L;

    // 机房位数 -> default -> 八机房
    private long dataCenterBitSize = 3;

    // 序列号占用的位数 -> default
    private long sequenceBitSize = 10;

    // 机器标识占用的位数 -> default -> 默认值为IP最后一段
    private long machineBitSize = 8;

    // 上次时间,一毫秒上限1024个ID,超过的时候错位处理
    private long lastTimeStamp = -1L;

    // 当前的ID
    private long sequence = 0L;

    // 数据中心编号或者说机房编号
    private long dataCenterId;

    // 机器标示,可以用IP低位区分
    private long machineId;

    private long maxDataCenterNum = ~(-1L << dataCenterBitSize);
    private long maxMachineNum = ~(-1L << machineBitSize);
    private long maxSequence = ~(-1L << sequenceBitSize);

    private long machineLeft = sequenceBitSize;
    private long dataCenterLeft = sequenceBitSize + machineBitSize;
    private long timeLeft = dataCenterLeft + dataCenterBitSize;

    public void setSequenceBitSize(long sequenceBitSize) {
        this.sequenceBitSize = sequenceBitSize;
        machineLeft = sequenceBitSize;
        maxSequence = ~(-1L << sequenceBitSize);
    }

    public void setMachineBitSize(long machineBitSize) {
        this.machineBitSize = machineBitSize;
        maxMachineNum = ~(-1L << machineBitSize);
        dataCenterLeft = sequenceBitSize + machineBitSize;
    }

    public void setDataCenterBitSize(long dataCenterBitSize) {
        this.dataCenterBitSize = dataCenterBitSize;
        maxDataCenterNum = ~(-1L << dataCenterBitSize);
        timeLeft = dataCenterLeft + dataCenterBitSize;
    }

    public void setDataCenterId(long dataCenterId) {
        if (dataCenterId > maxDataCenterNum || dataCenterId < 0) {
            throw new IllegalArgumentException("机房个数需要在[1,8]范围内");
        }
        this.dataCenterId = dataCenterId;
    }

    public void setMachineId(long machineId) {
        if (machineId > maxMachineNum || machineId < 0) {
            throw new IllegalArgumentException("每个机房机器编号范围为[1,254]");
        }
        this.machineId = machineId;
    }

    public void setLastTimeStamp(long lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    public synchronized long generator() {
        long currTime = System.currentTimeMillis();
        if (currTime == lastTimeStamp) {
            // 相同毫秒内，序列号自增
            sequence = (sequence + 1) & maxSequence;
            // 同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currTime = getNextMill();
            }
        }
        // 时间错等待,知道可以对外提供服务, 或者直接异常处理
        else if (currTime < lastTimeStamp) {
            currTime = getNextMill();
        }
        // 不同毫秒内，序列号置为0
        else {
            sequence = 0L;
        }
        lastTimeStamp = currTime;
        return (currTime - START_TIME_STAMP) << timeLeft // 时间戳部分
                | dataCenterId << dataCenterLeft // 数据中心部分
                | machineId << machineLeft // 机器标识部分
                | sequence; // 序列号部分
    }

    private long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastTimeStamp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }
}
