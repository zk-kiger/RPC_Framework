package com.kiger.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * @author zk_kiger
 * @date 2020/7/11
 */

public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses) {
        int random = new Random().nextInt(serviceAddresses.size());
        return serviceAddresses.get(random);
    }
}
