package com.nyquistdata.config;

import com.nyquistdata.config.client.EtcdPropertySource;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/5
 * @description 存放etcd key
 */
public class EtcdPropertySourceRepository {
    private final static ConcurrentHashMap<String, EtcdPropertySource> ETCD_PROPERTY_SOURCE_REPOSITORY = new ConcurrentHashMap<>();

    private final static ConcurrentHashMap<String,String> ETCD_DATA = new ConcurrentHashMap<>();

    private EtcdPropertySourceRepository(){

    }

    public static List<EtcdPropertySource> getAll(){
        return new ArrayList<>(ETCD_PROPERTY_SOURCE_REPOSITORY.values());
    }

    public static void collectEtcdPropertySource(EtcdPropertySource etcdPropertySource){
        ETCD_PROPERTY_SOURCE_REPOSITORY.putIfAbsent(etcdPropertySource.getDataId(),etcdPropertySource);
    }

    public static EtcdPropertySource getEtcdPropertySource(String dataId){
        return ETCD_PROPERTY_SOURCE_REPOSITORY.get(dataId);
    }

    public static void collectEtcdData(String dataId,String data){
        ETCD_DATA.put(dataId,data);
    }

    public static String getEtcdData(String dataId){
        return ETCD_DATA.get(dataId);
    }

    public static boolean isEmpty(){
        return CollectionUtils.isEmpty(ETCD_DATA);
    }
}
