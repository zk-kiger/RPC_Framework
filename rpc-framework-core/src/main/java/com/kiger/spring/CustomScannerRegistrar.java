package com.kiger.spring;

import com.kiger.annotation.RpcComponentScan;
import com.kiger.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * 扫描 @RpcComponentScan 指定包下的 @RpcService,@Component 注解
 *
 * 将 @RpcService,@Component 添加到 CustomScanner 的 includeFilters 集合中,
 * 后续调用 CustomScanner.scan() 对过滤注解的类的 BeanDefinition 添加到 registry
 *
 * @author zk_kiger
 * @date 2020/10/9
 */

@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final String SPRING_BEAN_BASE_PACKAGE = "com.kiger.spring";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        // 获取 @RpcComponentScan 注解属性
        AnnotationAttributes componentScanAnnotationAttributes =
                AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcComponentScan.class.getName()));
        String[] componentScanBasepackage = new String[0];
        if (componentScanAnnotationAttributes != null) {
            componentScanBasepackage = componentScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (componentScanBasepackage.length == 0) {
            componentScanBasepackage =
                    new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }

        // 创建扫描 RpcService 注解的 Scanner
        CustomScanner serviceCustomScanner = new CustomScanner(registry, RpcService.class);
        // 创建扫描 rpc-core 包中的 spring 组件的 Scanner
        CustomScanner componentCustomScanner = new CustomScanner(registry, Component.class);

        if (resourceLoader != null) {
            serviceCustomScanner.setResourceLoader(resourceLoader);
            componentCustomScanner.setResourceLoader(resourceLoader);
        }

        int springBeanAmount = componentCustomScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);
        int rpcServiceCount = serviceCustomScanner.scan(componentScanBasepackage);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);
    }
}
