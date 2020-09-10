package com.kiger.regist_discovery;

import com.kiger.factory.SingletonFactory;
import com.kiger.provider_centre.ServiceProvider;
import com.kiger.provider_centre.impl.CurrentHashMapServiceProviderImpl;
import com.kiger.regist_discovery.registry.ServiceRegistry;
import com.kiger.regist_discovery.registry.impl.ZKServiceRegistry;
import com.kiger.remoting.transport.netty.server.NettyServer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 暴露给服务提供方用于服务的注册
 *  1.nettyServer：需要为当前服务提供方创建一个 netty 实例
 *  2.serviceRegistry：将暴露的服务注册到注册中心（默认使用 zookeeper，用户可以自定义注册中心）
 *  3.serviceProvider:将暴露服务保存到服务提供中心，用于记录提供方所暴露的服务，当消费方
 *      通过网络传输找到服务提供方时，提供方可以直接从服务提供中心获取服务类型，直接执行方法返回结果
 * @author zk_kiger
 * @date 2020/7/10
 */

@Slf4j
@Data
public class ServiceRegister {
    private NettyServer nettyServer;
    private ServiceRegistry serviceRegistry;
    private ServiceProvider serviceProvider;

    public ServiceRegister(String host, int port) {
        // TODO 将 netty 的 host 和 port 可改为可配置的
        nettyServer = new NettyServer(host, port);
        serviceProvider = SingletonFactory.getInstance(CurrentHashMapServiceProviderImpl.class);
        serviceRegistry = new ZKServiceRegistry();
    }

    /**
     * 发布一个服务
     *  1.将服务保存到服务提供方的提供中心
     *  2.将服务注册到注册中心，便于消费者发现
     *  3.开启服务提供方的 netty 通道
     *
     * @param service      服务实例
     * @param serviceClass 服务Class类
     */
    public <T> void publishService(T service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service, serviceClass);
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(nettyServer.getHost(), nettyServer.getPort()));
        nettyServer.start();
    }
}
