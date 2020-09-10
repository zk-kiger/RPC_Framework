package com.kiger.remoting.transport.netty.server;

import com.kiger.enumeration.RpcMessageTypeEnum;
import com.kiger.remoting.handler.RpcRequestHandler;
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
 * 服务方处理消息
 *
 * @author zk_kiger
 * @date 2020/7/10
 */

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;

    public NettyServerHandler() {
        this.rpcRequestHandler = new RpcRequestHandler();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("server receive msg: [{}] ", msg);
            RpcRequest rpcRequest = (RpcRequest) msg;
            if (rpcRequest.getRpcMessageTypeEnum() == RpcMessageTypeEnum.HEART_BEAT) {
                log.info("receive heat beat msg from client");
                return;
            }
            // 执行目标方法，并且返回执行结果
            Object result = rpcRequestHandler.handler(rpcRequest);
            if (result != null)
                log.info("server get result: [{}]", result.toString());
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                // 返回方法执行结果给消费端
                RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                log.error("not writable now, message dropped");
            }
        } finally {
            // 确保 ByteBuf 被释放，不然可能会有内存泄露问题
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 服务端读超时 - 30s 内没有收到客户端的心跳或者消息，关闭连接
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

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
