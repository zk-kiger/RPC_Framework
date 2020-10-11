package com.kiger.remoting.transport.netty.server;

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
 * Channel initialization on first client request
 *
 * @author zk_kiger
 * @date 2020/7/10
 */

public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private Serializer serializer;

    public ServerInitializer() {
        this.serializer = new KryoSerializer();
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // If no client request is received within 30 seconds, close the connection.
        pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(new RpcEncoder());
        pipeline.addLast(new RpcDecoder());
        pipeline.addLast(new NettyServerHandler());
    }
}
