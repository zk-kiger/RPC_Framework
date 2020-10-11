package com.kiger.service.impl;

import com.kiger.annotation.RpcService;
import com.kiger.service.HelloService;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务提供方对服务的实现
 * @author zk_kiger
 * @date 2020/7/10
 */

@Slf4j
public class HelloServiceImpl2 implements HelloService {

    public void hello(String msg) {
        log.info("消费方成功调用服务2..");
        System.out.println("消费端调用服务2，消息内容为：[" + msg + "]");
    }

    @Override
    public String send(String msg) {
        log.info("消费方成功调用服务2..");
        System.out.println("消费端调用服务2，消息内容为：[" + msg + "]");
        return msg;
    }
}
