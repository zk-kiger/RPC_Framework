package com.kiger.regist_discovery.registry.impl;

import com.kiger.regist_discovery.registry.ServiceRegistry;
import com.kiger.utils.zookeeper.ZKClient;

import java.net.InetSocketAddress;

/**
 * 将服务注册到 zookeeper 服务中心
 * @author zk_kiger
 * @date 2020/7/10
 */

public class ZKServiceRegistry implements ServiceRegistry {

    @Override
    public void registerService(String serviceAllPathName, InetSocketAddress inetSocketAddress) {
        // 构建 zk 中的存储路径 - eg: /zk-rpc/com.kiger.service.HelloService/127.0.0.1:8888
        String path = ZKClient.ZK_REGISTER_ROOT_PATH + "/" + serviceAllPathName + inetSocketAddress.toString();
        ZKClient.createPersistentNode(path);
    }
}
