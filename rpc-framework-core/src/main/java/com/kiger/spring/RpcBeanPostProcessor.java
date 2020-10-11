package com.kiger.spring;

import com.kiger.annotation.RpcReference;
import com.kiger.annotation.RpcService;
import com.kiger.entity.RpcServiceProperties;
import com.kiger.proxy.RpcClientProxy;
import com.kiger.regist_discovery.ServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 在 bean 初始化之前如果父类标有 @RPCService 注解,就将当前类服务发布到注册中心
 * 在 bean 初始化之后如果存在 @RPCReference 字段就进行服务代理对象的属性注入
 *
 * @author zk_kiger
 * @date 2020/10/9
 */

@Slf4j
@Component
public class RpcBeanPostProcessor implements BeanPostProcessor {

    private final ServiceRegister serviceRegister;

    public RpcBeanPostProcessor() {
        this.serviceRegister = new ServiceRegister();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());

            // 获取 @RpcService 服务版本信息
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);

            // 构建 RpcServiceProperties
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().group(rpcService.group()).version(rpcService.version()).build();

            // 将服务信息保存到服务提供中心以及传输到服务注册中心
            serviceRegister.publishService(bean, rpcServiceProperties);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().group(rpcReference.group()).version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcServiceProperties);
                Object serviceProxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, serviceProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
