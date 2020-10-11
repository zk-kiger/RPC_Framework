package com.kiger;

import com.kiger.annotation.RpcComponentScan;
import com.kiger.controller.HelloController;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 1.Integrates Spring for service registration and consumption via annotations
 * 2.provides SPI mechanism (borrowed from Dubbo)
 * 3.refactored transport protocol encapsulation to RpcMessage
 * 4.solves TCP packet sticking and unpacking problem (with a given packet length)
 * using LengthFieldBasedFrameDecoder decoder.
 *
 * @author zk_kiger
 * @date 2020/10/11
 */

@RpcComponentScan(basePackage = "com.kiger")
public class ConsumerMainAnnotation {

    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext application = new AnnotationConfigApplicationContext(ConsumerMainAnnotation.class);
        HelloController helloController = (HelloController) application.getBean("helloController");
        helloController.test();
    }

}
