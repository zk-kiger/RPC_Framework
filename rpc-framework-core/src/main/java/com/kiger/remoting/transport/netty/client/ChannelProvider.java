package com.kiger.remoting.transport.netty.client;

import com.kiger.factory.SingletonFactory;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取与服务提供方的 Channel
 * @author zk_kiger
 * @date 2020/7/11
 */

public class ChannelProvider {

    private static Map<String, Channel> channels = new ConcurrentHashMap<>();
    private static NettyClient nettyClient;

    static {
        nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }

    private ChannelProvider() {}

    public static Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // 1.判断是否已经存在 channel
        if (channels.containsKey(key)) {
            // 2.判断 channel 是否可用
            Channel channel = channels.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            }
        }
        // 3.重新连接 channel
        Channel channel = nettyClient.doConnect(inetSocketAddress);
        channels.put(key, channel);
        return channel;
    }
}
