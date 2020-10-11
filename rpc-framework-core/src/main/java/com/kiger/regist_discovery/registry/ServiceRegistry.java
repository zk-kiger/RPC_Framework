package com.kiger.regist_discovery.registry;

import com.kiger.extension_spi.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册中心接口，默认使用 zookeeper
 * @author zk_kiger
 * @date 2020/7/10
 */

@SPI
public interface ServiceRegistry {

    /**
     * 注册服务
     *
     * @param serviceName       服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);

}
