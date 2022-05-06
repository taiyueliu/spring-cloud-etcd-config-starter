package com.nyquistdata.config;

import com.nyquistdata.config.refresh.EtcdContextRefresher;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
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
public class EtcdConfigAutoConfiguration {
    @Bean
    public EtcdConfigProperties etcdConfigProperties(ApplicationContext context){
        if (context.getParent() != null
                && BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                context.getParent(), EtcdConfigProperties.class).length > 0){
            return BeanFactoryUtils.beanOfTypeIncludingAncestors(context.getParent(),
                    EtcdConfigProperties.class);
        }
        return new EtcdConfigProperties();
    }

    @Bean
    public EtcdConfigManager etcdConfigManager(EtcdConfigProperties etcdConfigProperties){
        return new EtcdConfigManager(etcdConfigProperties);
    }

    @Bean
    public EtcdContextRefresher etcdContextRefresher(EtcdConfigManager etcdConfigManager){
        return new EtcdContextRefresher(etcdConfigManager);
    }
}
