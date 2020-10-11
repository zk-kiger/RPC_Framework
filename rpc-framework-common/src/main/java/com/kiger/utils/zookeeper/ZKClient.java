package com.kiger.utils.zookeeper;

import com.kiger.enumeration.RpcConfigEnum;
import com.kiger.exception.RpcException;
import com.kiger.utils.file.PropertiesFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zk_kiger
 * @date 2020/7/10
 */
@Slf4j
public final class ZKClient {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    public static final String ZK_REGISTER_ROOT_PATH = "/zk-rpc";

    // 相当于一层缓存，提高注册服务的效率
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    private static final CuratorFramework zkClient;

    static {
        zkClient = getZkClient();
    }

    private ZKClient() {
    }

    /**
     * 创建持久化节点。不同于临时节点，持久化节点不会因为客户端断开连接而被删除
     *
     * @param path 节点路径
     */
    public static void createPersistentNode(String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("节点已经存在，节点为:[{}]", path);
            } else {
                // eg: /zk-rpc/com.kiger.service.HelloService/127.0.0.1:8888
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("节点创建成功，节点为:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 获取某个节点下的子节点,也就是获取所有提供服务的生产者的地址
     *
     * @param serviceName 服务对象接口名 eg:com.kiger.service.HelloService
     * @return 指定字节下的所有子节点
     */
    public static List<String> getChildrenNodes(String serviceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(serviceName)) {
            return SERVICE_ADDRESS_MAP.get(serviceName);
        }
        List<String> result;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(serviceName, result);
            registerWatcher(zkClient, serviceName);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return result;
    }

    /**
     * 清空注册中心的数据
     */
    public static void clearRegistry() {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                zkClient.delete().forPath(p);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
        });
        log.info("服务端（Provider）所有注册的服务都被清空:[{}]", REGISTERED_PATH_SET.toString());
    }

    private static CuratorFramework getZkClient() {
        // check if user has set zk address
        Properties properties = PropertiesFileUtils.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        if (properties != null) {
            DEFAULT_ZOOKEEPER_ADDRESS = properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue());
        }

        // if zkClient has been started, return directly
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }

        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString(DEFAULT_ZOOKEEPER_ADDRESS)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        return zkClient;
    }

    /**
     * 注册监听指定节点。
     *
     * @param serviceName 服务对象接口名 eg:com.kiger.service.HelloService
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName) {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(serviceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

}
