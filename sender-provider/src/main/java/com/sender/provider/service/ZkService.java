package com.sender.provider.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.NetUtils;
import com.github.zkclient.ZkClient;
import com.sender.provider.constants.SenderConstants;

/**
 * @author haibo Date: 17-9-12 Time: 下午4:37
 */
@Service
public class ZkService implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkService.class);

    private ZkClient zkClient;

    @Value("${zk.url}")
    private String zkUrl;

    @Override
    public void afterPropertiesSet() throws Exception {
        zkClient = new ZkClient(zkUrl, 5000);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                if (!zkClient.exists(SenderConstants.ROOT_PATH)) {
                    zkClient.createPersistent(SenderConstants.ROOT_PATH, StringUtils.EMPTY.getBytes());
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            try {
                String appPath = SenderConstants.ROOT_PATH + NetUtils.getLocalHost();
                if (!zkClient.exists(appPath)) {
                    zkClient.createPersistent(appPath, Long.toString(System.currentTimeMillis()).getBytes());
                } else {
                    zkClient.writeData(appPath, Long.toString(System.currentTimeMillis()).getBytes());
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    /**
     * bean销毁的时候先断开连接
     */
    public void destroy() throws Exception {
        if (zkClient != null) {
            zkClient.close();
        }
    }
}