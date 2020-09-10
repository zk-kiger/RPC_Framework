package com.kiger.loadbalance;

import java.util.List;

/**
 * 负载均衡接口
 * @author zk_kiger
 * @date 2020/7/11
 */

public interface LoadBalance {

    /**
     * 在提供者服务地址列表中负载均衡选择一个
     */
    String selectServiceAddress(List<String> serviceAddresses);

}
