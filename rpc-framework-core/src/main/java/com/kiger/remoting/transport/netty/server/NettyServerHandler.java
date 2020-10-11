package com.kiger.remoting.transport.netty.server;

import com.kiger.enumeration.CompressTypeEnum;
import com.kiger.enumeration.RpcMessageTypeEnum;
import com.kiger.enumeration.RpcResponseCodeEnum;
import com.kiger.enumeration.SerializationTypeEnum;
import com.kiger.factory.SingletonFactory;
import com.kiger.remoting.constants.RpcConstants;
import com.kiger.remoting.handler.RpcRequestHandler;
import com.kiger.remoting.to.RpcMessage;
import com.kiger.remoting.to.RpcRequest;
import com.kiger.remoting.to.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Customize the ChannelHandler of the server to process the data sent by the client.
 *
 * @author zk_kiger
 * @date 2020/7/10
 */

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    log.info("receive heat beat msg from client");
                    RpcMessage rpcMessage = RpcMessage.builder()
                            .codec(SerializationTypeEnum.KYRO.getCode())
                            .compress(CompressTypeEnum.GZIP.getCode())
                            .messageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE)
                            .data(RpcConstants.PONG).build();
                    ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    // Execute the target method (the method the client needs to execute) and return the method result.
                    Object result = rpcRequestHandler.handler(rpcRequest);
                    log.info("server get result: [{}]", result.toString());
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        RpcMessage rpcMessage = RpcMessage.builder()
                                .codec(SerializationTypeEnum.KYRO.getCode())
                                .compress(CompressTypeEnum.GZIP.getCode())
                                .messageType(RpcConstants.RESPONSE_TYPE)
                                .data(rpcResponse).build();
                        ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        RpcMessage rpcMessage = RpcMessage.builder()
                                .codec(SerializationTypeEnum.KYRO.getCode())
                                .compress(CompressTypeEnum.GZIP.getCode())
                                .messageType(RpcConstants.RESPONSE_TYPE)
                                .data(rpcResponse).build();
                        ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        log.error("not writable now, message dropped");
                    }
                }
            }
        } finally {
            // Ensure that ByteBuf is released, otherwise there may be memory leaks!
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * Server read timeout - no client heartbeat or message received for 30s, close connection
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
