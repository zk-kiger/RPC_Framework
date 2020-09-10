package com.kiger.remoting.transport.netty.server;

import com.kiger.remoting.to.RpcRequest;
import com.kiger.remoting.to.RpcResponse;
import com.kiger.remoting.transport.netty.codec.RpcDecoder;
import com.kiger.remoting.transport.netty.codec.RpcEncoder;
import com.kiger.serialize.Serializer;
import com.kiger.serialize.kyro.KryoSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 客户端第一次请求时进行通道初始化
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
        // 30 秒之内没有收到客户端请求的话就关闭连接
        pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(new RpcDecoder(serializer, RpcRequest.class));
        pipeline.addLast(new RpcEncoder(serializer, RpcResponse.class));
        pipeline.addLast(new NettyServerHandler());
    }
}
