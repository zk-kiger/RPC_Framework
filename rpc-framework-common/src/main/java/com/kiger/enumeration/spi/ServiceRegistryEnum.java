package com.kiger.enumeration.spi;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zk_kiger
 * @date 2020/10/10
 */

@AllArgsConstructor
@Getter
public enum ServiceRegistryEnum {

    ZOOKEEPER("zk");

    private final String name;

}
