package com.nyquistdata.config.parser;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description Nacos-specific loader, If need to support other methods of parsing,you need to do the
 * following steps:
 * <p>
 *  1.inherit {@link AbstractPropertySourceLoader} ;<br/>
 *  2. define the file{@code spring.factories} and append
 *  {@code org.springframework.boot.env.PropertySourceLoader=..}; <br/>
 *  3.the last step validate.
 * </p>
 */
public abstract class AbstractPropertySourceLoader implements PropertySourceLoader {
    static final String DOT = ".";

    /**
     * Prevent interference with other loaders.Nacos-specific loader, unless the reload
     * changes it.
     * @param name the root name of the property source. If multiple documents are loaded
     * an additional suffix should be added to the name for each source loaded.
     * @param resource the resource to load
     * @return if the resource can be loaded
     */
    protected boolean canLoad(String name, Resource resource) {
        return resource instanceof EtcdByteArrayResource;
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
    public List<PropertySource<?>> load(String name, Resource resource)
            throws IOException {
        if (!canLoad(name, resource)) {
            return Collections.emptyList();
        }
        return this.doLoad(name, resource);
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
    protected abstract List<PropertySource<?>> doLoad(String name, Resource resource)
            throws IOException;

    protected void flattenedMap(Map<String, Object> result, Map<String, Object> dataMap,
                                String parentKey) {
        if (dataMap == null || dataMap.isEmpty()) {
            return;
        }
        Set<Map.Entry<String, Object>> entries = dataMap.entrySet();
        for (Iterator<Map.Entry<String, Object>> iterator = entries.iterator(); iterator
                .hasNext();) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();

            String fullKey = StringUtils.isEmpty(parentKey) ? key : key.startsWith("[")
                    ? parentKey.concat(key) : parentKey.concat(DOT).concat(key);

            if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                flattenedMap(result, map, fullKey);
                continue;
            }
            else if (value instanceof Collection) {
                int count = 0;
                Collection<Object> collection = (Collection<Object>) value;
                for (Object object : collection) {
                    flattenedMap(result,
                            Collections.singletonMap("[" + (count++) + "]", object),
                            fullKey);
                }
                continue;
            }

            result.put(fullKey, value);
        }
    }

}
