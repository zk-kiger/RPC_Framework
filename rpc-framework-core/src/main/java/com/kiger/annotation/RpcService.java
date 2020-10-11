package com.kiger.annotation;

import java.lang.annotation.*;

/**
 * RPC service annotation
 * 服务提供方标记在需要向外提供服务的服务上
 *
 * @author zk_kiger
 * @date 2020/10/9
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    String version() default "";

    String group() default "";

}
