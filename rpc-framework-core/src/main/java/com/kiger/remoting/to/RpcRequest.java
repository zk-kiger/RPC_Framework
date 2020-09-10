package com.kiger.remoting.to;

import com.kiger.enumeration.RpcMessageTypeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * @author zk_kiger
 * @date 2020/7/11
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private RpcMessageTypeEnum rpcMessageTypeEnum;
}
