package com.kiger.proxy;

import com.kiger.entity.RpcServiceProperties;
import com.kiger.enumeration.spi.ClientTransportEnum;
import com.kiger.extension_spi.ExtensionLoader;
import com.kiger.factory.SingletonFactory;
import com.kiger.remoting.to.RpcMessageChecker;
import com.kiger.remoting.to.RpcRequest;
import com.kiger.remoting.to.RpcResponse;
import com.kiger.remoting.transport.ClientTransport;
import com.kiger.remoting.transport.netty.client.NettyClientTransport;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 动态代理类。当动态代理对象调用一个方法的时候，实际调用的是下面的 invoke 方法。
 * 正是因为动态代理才让客户端调用的远程方法像是调用本地方法一样（屏蔽了中间过程）
 * @author zk_kiger
 * @date 2020/7/11
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    /**
     * use client send rpcRequest to the server.And there has two implementations: socket and netty.
     */
    private final ClientTransport clientTransport;

    private final RpcServiceProperties rpcServiceProperties;

    public RpcClientProxy() {
        this.clientTransport =
                ExtensionLoader.getExtensionLoader(ClientTransport.class).getExtension(ClientTransportEnum.NETTY_CLIENT_TRANSPORT.getName());
        this.rpcServiceProperties = null;
    }

    public RpcClientProxy(RpcServiceProperties rpcServiceProperties) {
        this.clientTransport = ExtensionLoader.getExtensionLoader(ClientTransport.class).getExtension(ClientTransportEnum.NETTY_CLIENT_TRANSPORT.getName());
        if (rpcServiceProperties.getGroup() == null) {
            rpcServiceProperties.setGroup("");
        }
        if (rpcServiceProperties.getVersion() == null) {
            rpcServiceProperties.setVersion("");
        }
        this.rpcServiceProperties = rpcServiceProperties;
    }

    /**
     * 通过 Proxy.newProxyInstance() 方法获取某个类的代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clz) {
        return (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class<?>[]{clz}, this);
    }

    /**
     * 使用代理对象调用方法的时候实际会调用到这个方法。代理对象就是你通过上面的 getProxy 方法获取到的对象。
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceProperties.getGroup())
                .version(rpcServiceProperties.getVersion())
                .build();
        RpcResponse<Object> rpcResponse = null;

        if (clientTransport instanceof NettyClientTransport) {
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) clientTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        }

        // 校验 RpcResponse 和 RpcRequest
        RpcMessageChecker.check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }
}
