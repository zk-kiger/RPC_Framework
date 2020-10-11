package com.kiger.enumeration.spi;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zk_kiger
 * @date 2020/10/10
 */

@AllArgsConstructor
@Getter
public enum ClientTransportEnum {

    NETTY_CLIENT_TRANSPORT("nettyClientTransport"),
    SOCKET_RPC_TRANSPORT("socketRpcTransport");

    private final String name;

}
