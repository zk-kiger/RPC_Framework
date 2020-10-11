package com.kiger.service;

/**
 * 服务提供方向外暴露的服务
 * @author zk_kiger
 * @date 2020/7/10
 */

public interface HelloService {

    void hello(String msg);

    String send(String msg);

}
