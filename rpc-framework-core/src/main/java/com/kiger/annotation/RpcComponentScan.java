package com.kiger.annotation;

import com.kiger.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC component scan annotation
 * 指定服务提供方服务所在路径
 *
 * @author zk_kiger
 * @date 2020/10/9
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Import(CustomScannerRegistrar.class)
public @interface RpcComponentScan {

    String[] basePackage();
}
