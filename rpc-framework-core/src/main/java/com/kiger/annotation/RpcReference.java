package com.kiger.annotation;

import java.lang.annotation.*;

/**
 * RPC reference annatation
 * 服务消费方自动获取 Service 实例对象(通过代理对象向服务提供方发送调用信息,获取调用结果)
 *
 * @author zk_kiger
 * @date 2020/10/9
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    String version() default "";

    String group() default "";

}
