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
 */
public abstract class AbstractPropertySourceLoader implements PropertySourceLoader {
    static final String DOT = ".";


    protected boolean canLoad(String name, Resource resource) {
        return resource instanceof EtcdByteArrayResource;
    }


    @Override
    public List<PropertySource<?>> load(String name, Resource resource)
            throws IOException {
        if (!canLoad(name, resource)) {
            return Collections.emptyList();
        }
        return this.doLoad(name, resource);
    }


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
