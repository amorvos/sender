package com.sender.client.generator.local;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;

public class TimeIpIdGenerator {

    // 2017-09-06 10:31:33.464 -> 170906.103133
    private static final int[] INDEXES = { 2, 3, 5, 6, 8, 9, 19, 11, 12, 14, 15, 17, 18 };

    // 干扰位,当前系统发送的消息数目
    private static final AtomicInteger messageOrder = new AtomicInteger(0);

    // 本地IP地址
    private static final String LOCAL_HOST = NetUtils.getLocalHost();

    // 在生成message id的时候带上进程id，避免一台机器上部署多个服务都发同样的消息时出问题
    private static final int PID = ConfigUtils.getPid();

    public String generatorId() {
        StringBuilder builder = new StringBuilder(40);
        long time = System.currentTimeMillis();
        String timeStamp = new Timestamp(time).toString();
        for (int idx : INDEXES) {
            builder.append(timeStamp.charAt(idx));
        }
        builder.append('.').append(LOCAL_HOST);
        builder.append('.').append(PID);
        builder.append('.').append(messageOrder.getAndIncrement());
        return builder.toString();
    }
}
