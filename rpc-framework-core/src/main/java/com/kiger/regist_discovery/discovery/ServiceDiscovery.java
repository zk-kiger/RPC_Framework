package com.kiger.regist_discovery.discovery;

import com.kiger.extension_spi.SPI;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 * @author zk_kiger
 * @date 2020/7/11
 */

@SPI
public interface ServiceDiscovery {
    /**
     * 查找服务
     *
     * @param serviceName 服务名称
     * @return 提供服务的地址
     */
    InetSocketAddress lookupService(String serviceName);
}
