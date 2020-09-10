package com.kiger.provider_centre.impl;

import com.kiger.enumeration.RpcErrorMessageEnum;
import com.kiger.exception.RpcException;
import com.kiger.provider_centre.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 CurrentHashMap 来实现服务提供方，服务的保存
 * @author zk_kiger
 * @date 2020/7/10
 */

@Slf4j
public class CurrentHashMapServiceProviderImpl implements ServiceProvider {

    /**
     * 接口名和服务的对应关系
     * TODO 一个接口被多个实现类实现
     * key:service/interface name
     * value:service
     */
    private static Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static Set<String> registeredService = ConcurrentHashMap.newKeySet();


    @Override
    public <T> void addServiceProvider(T service, Class<T> serviceClass) {
        String serviceName = serviceClass.getCanonicalName();
        if (registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        serviceMap.put(serviceName, service);
        log.info("ServiceProvider Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object serviceClass = serviceMap.get(serviceName);
        if (serviceClass == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return serviceClass;
    }
}
