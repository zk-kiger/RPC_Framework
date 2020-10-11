package com.kiger.remoting.to;

import lombok.*;

/**
 * @author zk_kiger
 * @date 2020/10/9
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    // rpc message type
    private byte messageType;
    // serialization type
    private byte codec;
    // compress type
    private byte compress;
    // request id
    private int requestId;
    // request data
    private Object data;

}
