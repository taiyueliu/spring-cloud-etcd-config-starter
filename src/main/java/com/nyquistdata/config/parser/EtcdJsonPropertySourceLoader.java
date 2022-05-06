package com.nyquistdata.config.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description json
 */
public class EtcdJsonPropertySourceLoader extends AbstractPropertySourceLoader{

    /**
     * constant.
     */
    private static final String VALUE = "value";

    /**
     * Returns the file extensions that the loader supports (excluding the '.').
     * @return the file extensions
     */
    @Override
    public String[] getFileExtensions() {
        return new String[] { "json" };
    }

    /**
     * Load the resource into one or more property sources. Implementations may either
     * return a list containing a single source, or in the case of a multi-document format
     * such as yaml a source for each document in the resource.
     * @param name the root name of the property source. If multiple documents are loaded
     * an additional suffix should be added to the name for each source loaded.
     * @param resource the resource to load
     * @return a list property sources
     * @throws IOException if the source cannot be loaded
     */
    @Override
    protected List<PropertySource<?>> doLoad(String name, Resource resource)
            throws IOException {
        Map<String, Object> result = new LinkedHashMap<>(32);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> nacosDataMap = mapper.readValue(resource.getInputStream(),
                LinkedHashMap.class);
        flattenedMap(result, nacosDataMap, null);
        return Collections.singletonList(
                new OriginTrackedMapPropertySource(name, this.reloadMap(result), true));

    }

    /**
     * Reload the key ending in `value` if need.
     */
    protected Map<String, Object> reloadMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>(map);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.contains(DOT)) {
                int idx = key.lastIndexOf(DOT);
                String suffix = key.substring(idx + 1);
                if (VALUE.equalsIgnoreCase(suffix)) {
                    result.put(key.substring(0, idx), entry.getValue());
                }
            }
        }
        return result;
    }

}
