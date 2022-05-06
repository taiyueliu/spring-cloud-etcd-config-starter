package com.nyquistdata.config.client;

import com.nyquistdata.config.EtcdPropertySourceRepository;
import com.nyquistdata.config.parser.EtcdDataParserHandler;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description todo
 */
public class EtcdPropertySourceBuilder {
    private static final Logger log = LoggerFactory
            .getLogger(EtcdPropertySourceBuilder.class);
    private KV kv;
    public EtcdPropertySourceBuilder(KV kv){
        this.kv = kv;
    }

    public KV getKv() {
        return kv;
    }

    public void setKv(KV kv) {
        this.kv = kv;
    }

    EtcdPropertySource build(String dataId, String fileExtension,
                             boolean isRefreshable){
        List<PropertySource<?>> propertySources = loadEtcdData(dataId,
                fileExtension);
        EtcdPropertySource etcdPropertySource = new EtcdPropertySource(dataId, isRefreshable, propertySources);
        EtcdPropertySourceRepository.collectEtcdPropertySource(etcdPropertySource);
        return etcdPropertySource;
    }

    private List<PropertySource<?>> loadEtcdData(String dataId,
                                                  String fileExtension) {
        String data = null;
        try {
            if(EtcdPropertySourceRepository.isEmpty() ){
                ByteSequence key = ByteSequence.from(dataId.getBytes());
                CompletableFuture<GetResponse> getFuture = kv.get(key);
                GetResponse response = getFuture.get();
                if(!CollectionUtils.isEmpty(response.getKvs())){
                    KeyValue keyValue = response.getKvs().get(0);
                    data = Optional.ofNullable(keyValue.getValue()).map(v -> v.toString(StandardCharsets.UTF_8)).orElse("");
                }
                return EtcdDataParserHandler.getInstance().parseEtcdData(dataId,data,fileExtension);
            }else if(StringUtils.hasLength(EtcdPropertySourceRepository.getEtcdData(dataId))){
                data = EtcdPropertySourceRepository.getEtcdData(dataId);
                return EtcdDataParserHandler.getInstance().parseEtcdData(dataId,data,fileExtension);
            }else{
                return Collections.emptyList();
            }
        }catch (Exception e){
            log.error("get data from etcd error,dataId:{} ", dataId, e);
        }
        return Collections.emptyList();
    }
}
