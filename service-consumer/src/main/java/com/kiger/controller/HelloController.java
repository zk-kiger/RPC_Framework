package com.kiger.controller;

import com.kiger.annotation.RpcReference;
import com.kiger.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HelloController {

    @RpcReference(group = "test1", version = "version1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            String msg = helloService.send("hello from consumer");
            log.info(msg);
        }
    }
}
