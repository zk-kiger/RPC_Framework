package com.kiger.remoting.transport.netty.codec;

import com.kiger.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 自定义解码器：负责处理"入站"消息，将消息格式转换为我们需要的业务对象
 * @author zk_kiger
 * @date 2020/7/11
 */

@Slf4j
@AllArgsConstructor
public class RpcDecoder extends ByteToMessageDecoder {
    private Serializer serializer;
    private Class<?> genericClass;

    /**
     * Netty传输的消息长度也就是对象序列化后对应的字节数组的大小，存储在 ByteBuf 头部
     */
    private static final int BODY_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 1.可读取字节大于等于4：存在消息内容 - 前4个字节保存字节数组的长度
        if (byteBuf.readableBytes() >= BODY_LENGTH) {
            // 2.标记当前readIndex的位置，以便后面重置 readIndex 的时候使用
            byteBuf.markReaderIndex();
            // 3.读取消息的长度
            int dataLength = byteBuf.readInt();
            // 4.遇到不合理的情况直接 return
            if (dataLength < 0 || byteBuf.readableBytes() < 0) {
                log.error("data length or byteBuf readableBytes is not valid");
                return;
            }
            // 5.如果可读字节数小于消息长度的话，说明是不完整的消息，重置readIndex
            if (byteBuf.readableBytes() < dataLength) {
                byteBuf.resetReaderIndex();
                return;
            }
            // 6.走到这里说明没什么问题了，可以序列化了
            byte[] body = new byte[dataLength];
            byteBuf.readBytes(body);
            // 将bytes数组转换为我们需要的对象
            Object obj = serializer.deserialize(body, genericClass);
            list.add(obj);
            log.info("successful decode ByteBuf to Object");
        }
    }
}
