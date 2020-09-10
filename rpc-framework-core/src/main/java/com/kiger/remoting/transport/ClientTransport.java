package com.kiger.remoting.transport;

import com.kiger.remoting.to.RpcRequest;

/**
 * 消费端发送消息到服务端（自定义实现传输方式：netty、socket..）
 * @author zk_kiger
 * @date 2020/7/11
 */

public interface ClientTransport {
    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
