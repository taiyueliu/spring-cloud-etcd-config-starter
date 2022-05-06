package com.nyquistdata.config.client;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/5
 * @description todo
 */
public class EtcdPropertySource extends MapPropertySource {
    private final String dataId;
    private final boolean isRefreshable;

    EtcdPropertySource(String dataId, boolean isRefreshable, Map<String, Object> source){
        super(dataId,source);
        this.dataId = dataId;
        this.isRefreshable = isRefreshable;
    }


    EtcdPropertySource(String dataId, boolean isRefreshable, List<PropertySource<?>> propertySources){
        this(dataId,isRefreshable,getSourceMap(dataId,propertySources));
    }

    private static Map<String, Object> getSourceMap(String dataId,
                                                    List<PropertySource<?>> propertySources) {
        if (CollectionUtils.isEmpty(propertySources)) {
            return Collections.emptyMap();
        }
        // If only one, return the internal element, otherwise wrap it.
        if (propertySources.size() == 1) {
            PropertySource propertySource = propertySources.get(0);
            if (propertySource != null && propertySource.getSource() instanceof Map) {
                return (Map<String, Object>) propertySource.getSource();
            }
        }
        // If it is multiple, it will be returned as it is, and the internal elements
        // cannot be directly retrieved, so the user needs to implement the retrieval
        // logic by himself
        return Collections.singletonMap(
                dataId,
                propertySources);
    }

    public String getDataId() {
        return dataId;
    }

    public boolean isRefreshable() {
        return isRefreshable;
    }
}
