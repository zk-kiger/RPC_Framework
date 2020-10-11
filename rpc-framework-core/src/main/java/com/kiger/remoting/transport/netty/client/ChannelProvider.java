package com.kiger.remoting.transport.netty.client;

import com.kiger.factory.SingletonFactory;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取与服务提供方的 Channel
 * @author zk_kiger
 * @date 2020/7/11
 */

@Slf4j
public class ChannelProvider {

    private static Map<String, Channel> channels = new ConcurrentHashMap<>();
    private static NettyClient nettyClient;

    static {
        nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }

    private ChannelProvider() {}

    public static Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // 1.Determine if it already exists channel
        if (channels.containsKey(key)) {
            // 2.Determine if a channel is available
            Channel channel = channels.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }
        // 3.Reconnecting the channel
        Channel channel = nettyClient.doConnect(inetSocketAddress);
        if (channel == null) {
            // The service provider is down, returns null, retry mechanism.
            return channel;
        }
        channels.put(key, channel);
        return channel;
    }
}
