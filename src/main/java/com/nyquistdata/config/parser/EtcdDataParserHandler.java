package com.nyquistdata.config.parser;

import com.nyquistdata.config.utils.EtcdConfigUtils;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.nyquistdata.config.parser.AbstractPropertySourceLoader.DOT;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description source handler
 */
public final class EtcdDataParserHandler {
    private static final String DEFAULT_EXTENSION = "properties";
    private static List<PropertySourceLoader> propertySourceLoaders;

    private EtcdDataParserHandler() {
        propertySourceLoaders = SpringFactoriesLoader
                .loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
    }

    public List<PropertySource<?>> parseEtcdData(String configName, String configValue,
                                                 String extension) throws IOException {
        if (!StringUtils.hasLength(configValue)) {
            return Collections.emptyList();
        }
        if (!StringUtils.hasLength(extension)) {
            extension = this.getFileExtension(configName);
        }
        for (PropertySourceLoader propertySourceLoader : propertySourceLoaders) {
            if (!canLoadFileExtension(propertySourceLoader, extension)) {
                continue;
            }
            EtcdByteArrayResource etcdByteArrayResource;
            if (propertySourceLoader instanceof PropertiesPropertySourceLoader) {
                etcdByteArrayResource = new EtcdByteArrayResource(EtcdConfigUtils.selectiveConvertUnicode(configValue).getBytes(),
                        configName);
            } else {
                etcdByteArrayResource = new EtcdByteArrayResource(configValue.getBytes(), configName);
            }

            etcdByteArrayResource.setFilename(getFileName(configName, extension));
            List<PropertySource<?>> propertySourceList = propertySourceLoader.load(configName, etcdByteArrayResource);
            if (CollectionUtils.isEmpty(propertySourceList)) {
                return Collections.emptyList();
            }
            return propertySourceList.stream().filter(Objects::nonNull)
                    .map(propertySource -> {
                        if (propertySource instanceof EnumerablePropertySource) {
                            String[] propertyNames = ((EnumerablePropertySource) propertySource)
                                    .getPropertyNames();
                            if (propertyNames.length > 0) {
                                Map<String, Object> map = new LinkedHashMap<>();
                                Arrays.stream(propertyNames).forEach(name -> {
                                    map.put(name, propertySource.getProperty(name));
                                });
                                return new OriginTrackedMapPropertySource(
                                        propertySource.getName(), map, true);
                            }
                        }
                        return propertySource;
                    }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean canLoadFileExtension(PropertySourceLoader loader, String extension) {
        return Arrays.stream(loader.getFileExtensions())
                .anyMatch((fileExtension) -> StringUtils.endsWithIgnoreCase(extension,
                        fileExtension));
    }

    public String getFileExtension(String name) {
        if (!StringUtils.hasLength(name)) {
            return DEFAULT_EXTENSION;
        }
        int idx = name.lastIndexOf(DOT);
        if (idx > 0 && idx < name.length() - 1) {
            return name.substring(idx + 1);
        }
        return DEFAULT_EXTENSION;
    }

    private String getFileName(String name, String extension) {
        if (!StringUtils.hasLength(extension)) {
            return name;
        }
        if (!StringUtils.hasLength(name)) {
            return extension;
        }
        int idx = name.lastIndexOf(DOT);
        if (idx > 0 && idx < name.length() - 1) {
            String ext = name.substring(idx + 1);
            if (extension.equalsIgnoreCase(ext)) {
                return name;
            }
        }
        return name + DOT + extension;
    }

    public static EtcdDataParserHandler getInstance() {
        return ParserHandler.HANDLER;
    }

    private static class ParserHandler {
        private static final EtcdDataParserHandler HANDLER = new EtcdDataParserHandler();
    }
}
