package com.kiger;

import com.kiger.regist_discovery.ServiceRegister;
import com.kiger.service.HelloService;
import com.kiger.service.impl.HelloServiceImpl;

/**
 * @author zk_kiger
 * @date 2020/7/10
 */

public class ProviderMain2 {
    public static void main(String[] args) {
        // 将暴露的服务注册到 zookeeper 上，同时包括当前提供方的 netty 地址
        // 1.手动注册,并且开启当前提供方的 netty 通道
        HelloService helloService = new HelloServiceImpl();
        ServiceRegister serviceRegister = new ServiceRegister("127.0.0.1", 9999);
        serviceRegister.publishService(helloService, HelloService.class);
    }
}
