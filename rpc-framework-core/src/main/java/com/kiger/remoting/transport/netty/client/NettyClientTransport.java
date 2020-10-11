package com.kiger.remoting.transport.netty.client;

import com.kiger.enumeration.CompressTypeEnum;
import com.kiger.enumeration.SerializationTypeEnum;
import com.kiger.enumeration.spi.ServiceDiscoveryEnum;
import com.kiger.enumeration.spi.ServiceRegistryEnum;
import com.kiger.extension_spi.ExtensionLoader;
import com.kiger.factory.SingletonFactory;
import com.kiger.regist_discovery.discovery.ServiceDiscovery;
import com.kiger.regist_discovery.discovery.impl.ZKServiceDiscovery;
import com.kiger.remoting.constants.RpcConstants;
import com.kiger.remoting.to.RpcMessage;
import com.kiger.remoting.to.RpcRequest;
import com.kiger.remoting.to.RpcResponse;
import com.kiger.remoting.transport.ClientTransport;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.applet.Applet;
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
        this.serviceDiscovery =
                ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZOOKEEPER.getName());
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        Object resultFuture = null;
        String rpcServiceName = rpcRequest.toRpcProperties().toRpcServiceName();

        // 1.获取到服务提供方的地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);

        // 2.获取服务地址 channel
        Channel channel = ChannelProvider.get(inetSocketAddress);

        if (channel != null && channel.isActive()) {
            resultFuture = getFutureResult(channel, rpcRequest);
        } else {
            // 3.出现服务提供方宕机,重试机制,重新获取服务提供方地址
            log.error("负载均衡获取服务提供方宕机,重试机制");
            int retryCount = 2;
            do {
                resultFuture = doRetry(rpcRequest);
                --retryCount;
            } while (resultFuture == null && retryCount > 0);

            if (resultFuture == null) {
                throw new IllegalStateException();
            }
        }

        return resultFuture;
    }

    /**
     * 服务提供方宕机,重试机制
     */
    Object doRetry(RpcRequest rpcRequest) {
        InetSocketAddress newInetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        Channel newChannel = ChannelProvider.get(newInetSocketAddress);
        if (newChannel != null && newChannel.isActive()) {
            return getFutureResult(newChannel, rpcRequest);
        } else {
            return null;
        }
    }

    /**
     * 将请求发送到服务端 channel,并异步获取结果
     * @param channel
     * @param rpcRequest
     * @return
     */
    Object getFutureResult(Channel channel, RpcRequest rpcRequest) {
        // 异步返回值
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

        // 放入未处理请求
        unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);

        RpcMessage rpcMessage = new RpcMessage().builder()
                .data(rpcRequest)
                .codec(SerializationTypeEnum.KYRO.getCode())
                .compress(CompressTypeEnum.GZIP.getCode())
                .messageType(RpcConstants.REQUEST_TYPE).build();

        channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("client send message: [{}]", rpcRequest);
            } else {
                future.channel().close();
                resultFuture.completeExceptionally(future.cause());
                log.error("Send failed:", future.cause());
            }
        });

        return resultFuture;
    }
}
