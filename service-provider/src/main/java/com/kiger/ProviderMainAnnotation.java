package com.kiger;

import com.kiger.annotation.RpcComponentScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

/**
 * @author zk_kiger
 * @date 2020/10/11
 */

@RpcComponentScan(basePackage = "com.kiger.service")
public class ProviderMainAnnotation {

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext application = new AnnotationConfigApplicationContext(ProviderMainAnnotation.class);
    }
}
