package com.kiger.provider_centre;

/**
 * 服务提供中心：可以使用不同的方式来实现该接口，默认使用 currentHashMap 来保存，
 *  也可以将这些信息保存到（Redis等第三方中间件）
 * @author zk_kiger
 * @date 2020/7/10
 */

public interface ServiceProvider {

    /**
     * 保存服务实例对象和服务实例对象实现的接口类的对应关系
     *
     * @param service      服务实例对象
     * @param serviceClass 服务实例对象实现的接口类
     * @param <T>          服务接口的类型
     */
    <T> void addServiceProvider(T service, Class<T> serviceClass);

    /**
     * 获取服务实例对象
     *
     * @param serviceName 服务实例对象实现的接口类的类名
     * @return 服务实例对象
     */
    Object getServiceProvider(String serviceName);

}
