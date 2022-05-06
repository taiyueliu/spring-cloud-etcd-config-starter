package com.nyquistdata.config;


import com.nyquistdata.config.analyzer.EtcdConnectionFailureException;
import io.etcd.jetcd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description etcd config manager
 */
public class EtcdConfigManager {
    private static final Logger log = LoggerFactory.getLogger(EtcdConfigManager.class);
    private static Client client;
    private static KV kv;
    private static Watch watch;
    private EtcdConfigProperties etcdConfigProperties;

    public EtcdConfigManager(EtcdConfigProperties etcdConfigProperties){
        this.etcdConfigProperties = etcdConfigProperties;
        createClient(etcdConfigProperties);
    }

    static Client createClient(EtcdConfigProperties etcdConfigProperties){
        if (Objects.isNull(client)) {
            synchronized(EtcdConfigManager.class){
                try {
                    if (Objects.isNull(client)) {
                        ClientBuilder clientBuilder = Client.builder()
                                .endpoints(etcdConfigProperties.getServerAddr().split(EtcdConfigProperties.COMMAS))
                                .waitForReady(false)
                                .connectTimeout(Duration.ofMillis(etcdConfigProperties.getConnectTimeout()));

                        if(StringUtils.hasLength(etcdConfigProperties.getUsername())){
                            clientBuilder.user(ByteSequence.from(etcdConfigProperties.getUsername(), StandardCharsets.UTF_8));
                        }
                        if(StringUtils.hasLength(etcdConfigProperties.getPassword())){
                            clientBuilder.password(ByteSequence.from(etcdConfigProperties.getPassword(), StandardCharsets.UTF_8));
                        }
                        if(StringUtils.hasLength(etcdConfigProperties.getNamespace())){
                            clientBuilder.namespace(ByteSequence.from(etcdConfigProperties.getNamespace(), StandardCharsets.UTF_8));
                        }
                        clientBuilder.loadBalancerPolicy("round_robin");
                        client = clientBuilder.build();
                        kv = client.getKVClient();
                        watch = client.getWatchClient();
                    }
                }catch (Exception e){
                    log.error(e.getMessage());
                    throw new EtcdConnectionFailureException(
                            etcdConfigProperties.getServerAddr(), e.getMessage(), e);
                }
            }
        }
        return client;
    }

    public Client getClient() {
        if (Objects.isNull(client)) {
            createClient(this.etcdConfigProperties);
        }

        return client;
    }

    public KV getKv(){
        if (Objects.isNull(client)) {
            createClient(this.etcdConfigProperties);
        }
        return kv;
    }

    public Watch getWatch(){
        if (Objects.isNull(client)) {
            createClient(this.etcdConfigProperties);
        }
        return watch;
    }

    public EtcdConfigProperties getEtcdConfigProperties(){
        return etcdConfigProperties;
    }
}
