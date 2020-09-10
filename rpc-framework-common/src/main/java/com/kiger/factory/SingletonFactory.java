package com.kiger.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取单例对象工厂类
 * @author zk_kiger
 * @date 2020/7/11
 */

public final class SingletonFactory {

    private static Map<String, Object> singleMap = new HashMap<>();

    private SingletonFactory() {}

    public static <T> T getInstance(Class<T> clz) {
        String key = clz.toString();
        Object instance = singleMap.get(key);
        synchronized (clz) {
            if (instance == null) {
                try {
                    instance = clz.newInstance();
                    singleMap.put(key, instance);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        // Class.cast()：强制转换类型
        return clz.cast(instance);
    }

}
