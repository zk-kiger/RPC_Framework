package com.kiger.remoting.transport.netty.client;


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
        // 如果 5 秒之内没有发送数据给服务端的话，就发送一次心跳请求
        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
        /*自定义序列化编解码器*/
        // RpcResponse -> ByteBuf
        pipeline.addLast(new RpcDecoder(serializer, RpcResponse.class));
        // ByteBuf -> RpcRequest
        pipeline.addLast(new RpcEncoder(serializer, RpcRequest.class));
        pipeline.addLast(new NettyClientHandler());
    }
}
