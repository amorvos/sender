package com.sender.provider.biz;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.NetUtils;
import com.google.common.base.Preconditions;
import com.sender.client.container.SenderConstant;
import com.sender.provider.constants.SenderConstants;
import com.sender.provider.service.SnowFlakeService;
import com.sender.provider.service.ZkService;

/**
 * @author haibo Date: 17-9-7 Time: 下午2:14
 */
@Service
@SuppressWarnings("unchecked")
public class IdGeneratorBiz implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private SnowFlakeService generator;

    @Resource
    private ZkService zkService;

    public void afterPropertiesSet() throws Exception {
        Object bean = applicationContext.getBean("snowFlakeService");
        if (!(bean instanceof SnowFlakeService)) {
            // 监控报警
            return;
        }
        SnowFlakeService snowFlakeService = (SnowFlakeService) bean;
        snowFlakeService.setMachineId(buildMachineId());
        snowFlakeService.setDataCenterId(buildDataCenterId());
        snowFlakeService.setLastTimeStamp(buildLastTimeStamp());
        generator = snowFlakeService;
    }

    private Long buildDataCenterId() {
        // 系统数据识别
        return 0L;
    }

    private Long buildMachineId() {
        List<String> segments = SenderConstant.POINT_SPLITTER.splitToList(NetUtils.getLocalHost());
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(segments), "HOST获取异常");
        return Long.parseLong(segments.get(3));
    }

    private Long buildLastTimeStamp() throws KeeperException, InterruptedException {
        // ZK 部分待实现
        String appPath = SenderConstants.ROOT_PATH + NetUtils.getLocalHost();
        long zkTime = Long.parseLong(new String(zkService.getZkClient().readData(appPath)));
        long minTime = zkTime + SenderConstants.SESSION_TIME_OUT.longValue();
        if (minTime > System.currentTimeMillis()) {
            // TODO 指标报警
            // 时间错位兼容
            return minTime;
        }
        return 0L;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public SnowFlakeService getGenerator() {
        return generator;
    }
}