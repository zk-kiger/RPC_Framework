package com.kiger.remoting.handler;

import com.kiger.enumeration.RpcResponseCodeEnum;
import com.kiger.exception.RpcException;
import com.kiger.factory.SingletonFactory;
import com.kiger.provider_centre.ServiceProvider;
import com.kiger.provider_centre.impl.CurrentHashMapServiceProviderImpl;
import com.kiger.remoting.to.RpcRequest;
import com.kiger.remoting.to.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RpcRequest 处理器
 * @author zk_kiger
 * @date 2020/7/11
 */

@Slf4j
public class RpcRequestHandler {

    private static ServiceProvider serviceProvider = SingletonFactory.getInstance(CurrentHashMapServiceProviderImpl.class);

    /**
     * 根据 rpcRequest 中的目标类和目标方法在服务提供中心找到目标类并且执行目标方法
     * @param rpcRequest    rpc请求类
     * @return              执行目标方法返回执行结果
     */
    public Object handler(RpcRequest rpcRequest) {
        // 通过服务提供中心获取目标类
        Object service = serviceProvider.getService(rpcRequest.toRpcProperties());
        // 调用目标类的目标方法
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 使用功反射调用目标类的目标方法，返回执行结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            if (method == null) {
                return RpcResponse.fail(RpcResponseCodeEnum.NOT_FOUND_METHOD);
            }
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }

}
