package com.kiger.remoting.transport.netty.client;

import com.kiger.factory.SingletonFactory;
import com.kiger.regist_discovery.discovery.ServiceDiscovery;
import com.kiger.regist_discovery.discovery.impl.ZKServiceDiscovery;
import com.kiger.remoting.to.RpcRequest;
import com.kiger.remoting.to.RpcResponse;
import com.kiger.remoting.transport.ClientTransport;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * 使用 netty 将消息发送给服务端
 * @author zk_kiger
 * @date 2020/7/11
 */

@Slf4j
public class NettyClientTransport implements ClientTransport {

    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;

    public NettyClientTransport() {
        this.serviceDiscovery = new ZKServiceDiscovery();
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 异步返回值
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        // 1.获取到服务提供方的地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        // 2.获取服务地址 channel
        Channel channel = ChannelProvider.get(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            // 放入未处理请求
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcRequest);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }
}
