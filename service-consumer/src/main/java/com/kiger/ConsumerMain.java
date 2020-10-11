package com.kiger;

import com.kiger.proxy.RpcClientProxy;
import com.kiger.service.HelloService;

/**
 *
 * TODO 继承spring，使注册中心，序列化方式变为可配置
 * @author zk_kiger
 * @date 2020/7/11
 */

public class ConsumerMain {
    public static void main(String[] args) {
        // 消费方只需要获取到代理对象执行方法即可
        HelloService helloService = new RpcClientProxy().getProxy(HelloService.class);
        helloService.hello("你好，提供方!");
        String result = helloService.send("你好，啦啦啦！");
        System.out.println(result);
    }
}
