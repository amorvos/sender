package com.sender.provider.zk;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.utils.NetUtils;
import com.github.zkclient.ZkClient;
import com.sender.provider.constants.SenderConstants;

/**
 * @author haibo Date: 17-9-13 Time: 上午10:03
 */
public class ZkClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkClientTest.class);

    private static ZkClient zkClient = new ZkClient("localhost:2181", 1000);

    @Test
    public void testClient() throws Exception {
        String path = "/zk-book";
        // 注册回调接口 Listener不是一次性的，注册一次就会一直生效
        zkClient.subscribeChildChanges(path, (parentPath, currentChildren) -> System.out
                .println(parentPath + " 's child changed, currentChildren:" + currentChildren));
        zkClient.delete(path);
        // 第一次创建当前节点，客户端会收到通知 /zk-book 's child changed, currentChildren:[]
        zkClient.createPersistent(path);
        Thread.sleep(1000);
        // 创建子节点，客户端会收到通知 /zk-book 's child changed, currentChildren:[c1]
        zkClient.createPersistent(path + "/c1");
        Thread.sleep(1000);
        // 删除子节点，客户端会收到通知 /zk-book 's child changed, currentChildren:[]
        zkClient.delete(path + "/c1");
        Thread.sleep(1000);
        // 删除当前节点，客户端会收到通知 /zk-book 's child changed, currentChildren:null
        zkClient.delete(path);
        Thread.sleep(1000);
    }

    @Test
    public void testZkClientUpdate() throws Exception {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        zkClient.subscribeChildChanges(SenderConstants.ROOT_PATH, (parentPath, currentChildren) -> System.out
                .println(parentPath + " 's child changed, currentChildren:" + currentChildren));
        try {
            if (!zkClient.exists(SenderConstants.ROOT_PATH)) {
                zkClient.createPersistent(SenderConstants.ROOT_PATH, StringUtils.EMPTY.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String appPath = SenderConstants.ROOT_PATH + NetUtils.getLocalHost();
        try {
            if (!zkClient.exists(appPath)) {
                zkClient.createPersistent(appPath, Long.toString(System.currentTimeMillis()).getBytes());
            } else {
                zkClient.writeData(appPath, Long.toString(System.currentTimeMillis()).getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.scheduleAtFixedRate(() -> {
            zkClient.writeData(appPath, Long.toString(System.currentTimeMillis()).getBytes());
        }, 0, 100, TimeUnit.MILLISECONDS);
        for (int i = 0; i < 10; i++) {
            zkClient.createEphemeralSequential(appPath, "".getBytes());
        }
        System.out.println("--------------");
    }
}