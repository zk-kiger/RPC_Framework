package com.kiger.remoting.transport.netty.codec;

import com.kiger.compress.Compress;
import com.kiger.enumeration.CompressTypeEnum;
import com.kiger.enumeration.SerializationTypeEnum;
import com.kiger.extension_spi.ExtensionLoader;
import com.kiger.remoting.constants.RpcConstants;
import com.kiger.remoting.to.RpcMessage;
import com.kiger.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom protocol encoder: write messages to ByteBuf in the following protocol format
 *
 *   0     1     2     3     4        5     6     7    8    9           10      11       12  13   14   15   16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId      |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 *  4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 *  1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 *  body（object类型数据）
 *
 * @author zk_kiger
 * @date 2020/7/11
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */

@AllArgsConstructor
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {

    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        try {
            // magic code
            byteBuf.writeBytes(RpcConstants.MAGIC_NUMBER);

            // version
            byteBuf.writeByte(RpcConstants.VERSION);

            // leave a place to write the value of full length
            byteBuf.writerIndex(byteBuf.writerIndex() + 4);

            // message type
            byte messageType = rpcMessage.getMessageType();
            byteBuf.writeByte(messageType);

            // codec
            byteBuf.writeByte(rpcMessage.getCodec());

            // compress
            byteBuf.writeByte(rpcMessage.getCompress());

            // request_id
            byteBuf.writeInt(REQUEST_ID.getAndIncrement());

            /** build full length */
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            // if messageType is not heartbeat message,fullLength = head length + body length
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                // serialize the data
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());

                // compress the bytes
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                byteBuf.writeBytes(bodyBytes);
            }
            int writeIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            byteBuf.writeInt(fullLength);
            byteBuf.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}
