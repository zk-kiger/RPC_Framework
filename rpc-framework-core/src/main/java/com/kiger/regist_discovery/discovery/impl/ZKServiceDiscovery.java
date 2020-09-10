package com.kiger.regist_discovery.discovery.impl;

import com.kiger.loadbalance.LoadBalance;
import com.kiger.loadbalance.RandomLoadBalance;
import com.kiger.regist_discovery.discovery.ServiceDiscovery;
import com.kiger.regist_discovery.registry.impl.ZKServiceRegistry;
import com.kiger.utils.zookeeper.ZKClient;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 使用 zookeeper 作为服务发现中心
 * @author zk_kiger
 * @date 2020/7/11
 */

@Slf4j
public class ZKServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZKServiceDiscovery() {
        this.loadBalance = new RandomLoadBalance();
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // 1.从 zk 上获取服务的地址列表
        List<String> serviceUrlList = ZKClient.getChildrenNodes(serviceName);
        // 2.负载均衡获取其中一个服务地址
        // eg: /zk-rpc/com.kiger.service.HelloService/127.0.0.1:8888
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList);
        log.info("成功找到服务地址:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
