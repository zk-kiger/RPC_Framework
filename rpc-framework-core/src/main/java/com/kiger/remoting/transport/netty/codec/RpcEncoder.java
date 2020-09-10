package com.kiger.remoting.transport.netty.codec;

import com.kiger.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * 自定义编码器：负责处理"出站"消息，将消息格式转换字节数组然后写入到字节数据的容器 ByteBuf 对象中。
 * @author zk_kiger
 * @date 2020/7/11
 */

@AllArgsConstructor
public class RpcEncoder extends MessageToByteEncoder<Object> {
    private Serializer serializer;
    private Class<?> genericClass;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            // 1.使用序列化器将对象转为 byte[]
            byte[] bytes = serializer.serialize(o);
            // 2.读取字节数组长度存入 buf 中，方便解码
            int dataLength = bytes.length;
            // 3.先向 buf ，写入字节长度
            byteBuf.writeInt(dataLength);
            // 4.将字节数组写入
            byteBuf.writeBytes(bytes);
        }
    }
}
