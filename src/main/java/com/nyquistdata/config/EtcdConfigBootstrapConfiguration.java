package com.nyquistdata.config;

import com.nyquistdata.config.client.EtcdPropertySourceLocator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description todo
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.etcd.config.enabled", matchIfMissing = true)
public class EtcdConfigBootstrapConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public EtcdConfigProperties etcdConfigProperties(){
        return new EtcdConfigProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public EtcdConfigManager etcdConfigManager(EtcdConfigProperties etcdConfigProperties){
        return new EtcdConfigManager(etcdConfigProperties);
    }

    @Bean
    public EtcdPropertySourceLocator etcdPropertySourceLocator(EtcdConfigManager etcdConfigManager){
        return new EtcdPropertySourceLocator(etcdConfigManager);
    }
}
