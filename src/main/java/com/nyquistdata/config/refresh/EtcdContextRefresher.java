package com.nyquistdata.config.refresh;

import com.nyquistdata.config.EtcdConfigManager;
import com.nyquistdata.config.EtcdConfigProperties;
import com.nyquistdata.config.EtcdPropertySourceRepository;
import com.nyquistdata.config.client.EtcdPropertySource;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description todo
 */
public class EtcdContextRefresher implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {
    private final static Logger log = LoggerFactory
            .getLogger(EtcdContextRefresher.class);
    private static final AtomicLong REFRESH_COUNT = new AtomicLong(0);

    private EtcdConfigProperties etcdConfigProperties;

    private final boolean isRefreshEnabled;

    private final Watch watch;

    private ApplicationContext applicationContext;

    private AtomicBoolean ready = new AtomicBoolean(false);

    private Map<String, Consumer<WatchResponse>> watchMap = new ConcurrentHashMap<>(16);


    public EtcdContextRefresher(EtcdConfigManager etcdConfigManager){
        this.etcdConfigProperties = etcdConfigManager.getEtcdConfigProperties();
        this.watch = etcdConfigManager.getWatch();
        this.isRefreshEnabled = this.etcdConfigProperties.isRefreshEnabled();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // many Spring context
        if (this.ready.compareAndSet(false, true)) {
            this.registerEtcdWatchForApplications();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private void registerEtcdWatchForApplications(){
        if(isRefreshEnabled){
            for (EtcdPropertySource propertySource : EtcdPropertySourceRepository.getAll()) {
                if (!propertySource.isRefreshable()) {
                    continue;
                }
                String dataId = propertySource.getDataId();
                registerEtcdWatch(dataId);
            }
        }
    }

    private void registerEtcdWatch(final String key){
        Consumer<WatchResponse> config = watchMap.computeIfAbsent(key, lst -> new Consumer<WatchResponse>() {
            @Override
            public void accept(WatchResponse watchResponse) {
                for (WatchEvent event : watchResponse.getEvents()) {
                    WatchEvent.EventType eventType = event.getEventType();
                    if (eventType == WatchEvent.EventType.PUT) {
                        refreshCountIncrement();
                        KeyValue keyValue = event.getKeyValue();
                        String data = Optional.ofNullable(keyValue.getValue()).map(v -> v.toString(StandardCharsets.UTF_8)).orElse("");
                        EtcdPropertySourceRepository.collectEtcdData(key,data);
                        applicationContext.publishEvent(
                                new RefreshEvent(this, null, "Refresh etcd config"));
                    }
                }
            }
        });
        try {
            ByteSequence byteSequence = ByteSequence.from(key, StandardCharsets.UTF_8);
            watch.watch(byteSequence,config);
        }catch (Exception e){
            log.warn(String.format(
                    "register fail for etcd watch ,dataId=[%s],group=[%s]", key), e);
        }
    }


    public boolean isRefreshEnabled() {
        if (null == etcdConfigProperties) {
            return isRefreshEnabled;
        }
        // Compatible with older configurations
        if (etcdConfigProperties.isRefreshEnabled() && !isRefreshEnabled) {
            return false;
        }
        return isRefreshEnabled;
    }

    public static long getRefreshCount() {
        return REFRESH_COUNT.get();
    }

    public static void refreshCountIncrement() {
        REFRESH_COUNT.incrementAndGet();
    }
}
