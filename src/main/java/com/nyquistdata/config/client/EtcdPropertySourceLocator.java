package com.nyquistdata.config.client;

import com.nyquistdata.config.EtcdConfigManager;
import com.nyquistdata.config.EtcdConfigProperties;
import com.nyquistdata.config.EtcdPropertySourceRepository;
import com.nyquistdata.config.refresh.EtcdContextRefresher;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description 初始化或者refresh event时触发
 */
@Order(0)
public class EtcdPropertySourceLocator implements PropertySourceLocator {
    private static final Logger log = LoggerFactory
            .getLogger(EtcdPropertySourceLocator.class);

    private static final String ETCD_PROPERTY_SOURCE_NAME = "ETCD";

    private static final String SEP1 = "-";

    private static final String DOT = ".";

    private EtcdPropertySourceBuilder etcdPropertySourceBuilder;

    private EtcdConfigProperties etcdConfigProperties;

    private EtcdConfigManager etcdConfigManager;

    public EtcdPropertySourceLocator(EtcdConfigManager etcdConfigManager){
        this.etcdConfigManager = etcdConfigManager;
        this.etcdConfigProperties = etcdConfigManager.getEtcdConfigProperties();
    }

    @Override
    public PropertySource<?> locate(Environment env) {
        etcdConfigProperties.setEnvironment(env);
        KV kv = etcdConfigManager.getKv();
        if (null == kv) {
            log.warn("no instance of client found, can't load config from etcd");
            return null;
        }
        etcdPropertySourceBuilder = new EtcdPropertySourceBuilder(kv);

        String name = etcdConfigProperties.getName();
        String dataIdPrefix = etcdConfigProperties.getPrefix();
        if(!StringUtils.hasLength(dataIdPrefix)){
            dataIdPrefix = name;
        }
        if(!StringUtils.hasLength(dataIdPrefix)){
            dataIdPrefix = env.getProperty("spring.application.name");
        }
        CompositePropertySource composite = new CompositePropertySource(ETCD_PROPERTY_SOURCE_NAME);

        loadApplicationConfiguration(composite,dataIdPrefix,etcdConfigProperties,env);
        return composite;
    }

    private void loadApplicationConfiguration(CompositePropertySource compositePropertySource, String dataIdPrefix,
                                              EtcdConfigProperties properties, Environment environment){
        String fileExtension = properties.getFileExtension();
        // load directly once by default
        loadEtcdDataIfPresent(compositePropertySource, dataIdPrefix,
                fileExtension, properties.isRefreshEnabled());

        loadEtcdDataIfPresent(compositePropertySource,
                dataIdPrefix + DOT + fileExtension, fileExtension, true);

        for (String profile : environment.getActiveProfiles()) {
            String dataId = dataIdPrefix + SEP1 + profile + DOT + fileExtension;
            loadEtcdDataIfPresent(compositePropertySource, dataId,
                    fileExtension, properties.isRefreshEnabled());
        }
    }

    private void loadEtcdDataIfPresent(final CompositePropertySource composite,
                                        final String dataId, String fileExtension,
                                        boolean isRefreshable) {
        if (null == dataId || dataId.trim().length() < 1) {
            return;
        }
        EtcdPropertySource propertySource = this.loadEtcdPropertySource(dataId, fileExtension, isRefreshable);
        this.addFirstPropertySource(composite, propertySource, false);
    }

    private EtcdPropertySource loadEtcdPropertySource(final String dataId, String fileExtension,
                                                      boolean isRefreshable) {
        if(EtcdContextRefresher.getRefreshCount() != 0){
            if(!isRefreshable){
                return EtcdPropertySourceRepository.getEtcdPropertySource(dataId);
            }
        }

        return etcdPropertySourceBuilder.build(dataId,fileExtension,isRefreshable);
    }

    private void addFirstPropertySource(final CompositePropertySource composite,
                                        EtcdPropertySource etcdPropertySource,boolean ignoreEmpty){
        if(null == etcdPropertySource || null == composite){
            return;
        }
        if(ignoreEmpty && etcdPropertySource.getSource().isEmpty()){
            return;
        }

        composite.addFirstPropertySource(etcdPropertySource);
    }
}
