package com.kiger.remoting.transport.netty.codec;

import com.kiger.compress.Compress;
import com.kiger.enumeration.CompressTypeEnum;
import com.kiger.enumeration.SerializationTypeEnum;
import com.kiger.extension_spi.ExtensionLoader;
import com.kiger.remoting.constants.RpcConstants;
import com.kiger.remoting.to.RpcMessage;
import com.kiger.remoting.to.RpcRequest;
import com.kiger.remoting.to.RpcResponse;
import com.kiger.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * custom protocol decoder
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
 * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 *
 * @author zk_kiger
 * @date 2020/7/11
 */

@Slf4j
public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    public RpcDecoder() {
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }


    /**
     * Ensure that lengthFieldOffset + lengthFieldLength + (fullLength + lengthAdjustment) = length of the packet
     * being sent to get the desired packet content by initialBytesToStrip.
     *
     * @param maxFrameLength      Maximum frame length. It decide the maximum length of data that can be received.
     *                            If it exceeds, the data will be discarded.
     * @param lengthFieldOffset   Length field offset. The length field is the one that skips the specified length of byte.
     * @param lengthFieldLength   The number of bytes in the length field.
     * @param lengthAdjustment    The compensation value to add to the value of the length field
     * @param initialBytesToStrip Number of bytes skipped.
     *                            If you need to receive all of the header+body data, this value is 0
     *                            if you only want to receive the body data, then you need to skip the number of bytes consumed by the header.
     */
    public RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // By setting the parameter LengthFieldBasedFrameDecoder, it is guaranteed
        // that no TCP packet splitting or sticking will occur when parsing to a complete packet.
        Object decoded = super.decode(ctx, in);

        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() > RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }

        return null;
    }

    /**
     * Parsing packets
     */
    private Object decodeFrame(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] temp = new byte[len];
        in.readBytes(temp);
        for (int i = 0; i < len; i++) {
            if (temp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(temp));
            }
        }

        // read the version and compare
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
        int fullLength = in.readInt();

        // build RpcMessage object
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .compress(compressType)
                .requestId(requestId)
                .messageType(messageType).build();

        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
        } else if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
        } else {
            int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
            if (bodyLength > 0) {
                byte[] bodyBytes = new byte[bodyLength];
                in.readBytes(bodyBytes);

                // decompress the bytes
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                bodyBytes = compress.decompress(bodyBytes);

                // deserialize the object
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
                if (messageType == RpcConstants.REQUEST_TYPE) {
                    RpcRequest rpcRequest = serializer.deserialize(bodyBytes, RpcRequest.class);
                    rpcMessage.setData(rpcRequest);
                } else {
                    RpcResponse rpcResponse = serializer.deserialize(bodyBytes, RpcResponse.class);
                    rpcMessage.setData(rpcResponse);
                }
            }
        }

        return rpcMessage;
    }
}
