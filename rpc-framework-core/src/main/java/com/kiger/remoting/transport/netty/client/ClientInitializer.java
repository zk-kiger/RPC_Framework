package com.kiger.remoting.transport.netty.client;

import com.kiger.remoting.transport.netty.codec.RpcDecoder;
import com.kiger.remoting.transport.netty.codec.RpcEncoder;
import com.kiger.serialize.Serializer;
import com.kiger.serialize.kryo.KryoSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author zk_kiger
 * @date 2020/7/11
 */

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private Serializer serializer;

    public ClientInitializer() {
        this.serializer = new KryoSerializer();
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // If no data is sent to the server within 5 seconds, a heartbeat request is sent.
        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));

        /* Custom serialization codecs */
        pipeline.addLast(new RpcEncoder());
        pipeline.addLast(new RpcDecoder());
        pipeline.addLast(new NettyClientHandler());
    }
}
