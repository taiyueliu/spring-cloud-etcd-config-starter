package com.nyquistdata.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/5
 * @description etcd properties
 */
@ConfigurationProperties(EtcdConfigProperties.PREFIX)
public class EtcdConfigProperties {
    /**
     * Prefix of
     */
    public static final String PREFIX = "spring.cloud.etcd.config";

    private static final Logger log = LoggerFactory
            .getLogger(EtcdConfigProperties.class);

    public static final String COMMAS = ",";

    /**
     * v3地址 http://localhost:2379, http://localhost:2389
     */
    private String serverAddr;

    private String username;

    private String password;

    private String namespace;

    private String prefix;

    private String fileExtension = "properties";

    private String name;

    private long connectTimeout = 10000;

    private boolean refreshEnabled = true;


    @JsonIgnore
    @Resource
    private Environment environment;

    @PostConstruct
    public void init() {
        this.overrideFromEnv();
    }

    private void overrideFromEnv() {
        if (!StringUtils.hasLength(this.getServerAddr())) {
            String serverAddr = environment
                    .resolvePlaceholders("${spring.cloud.etcd.config.server-addr:}");
           if(!StringUtils.hasLength(serverAddr)){
               serverAddr = environment.resolvePlaceholders(
                       "${spring.cloud.etcd.server-addr:http://localhost:2379}");
           }
            this.setServerAddr(serverAddr);
        }
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRefreshEnabled() {
        return refreshEnabled;
    }

    public void setRefreshEnabled(boolean refreshEnabled) {
        this.refreshEnabled = refreshEnabled;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "EtcdConfigProperties{" +
                "serverAddr='" + serverAddr + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", namespace='" + namespace + '\'' +
                ", prefix='" + prefix + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", name='" + name + '\'' +
                ", connectTimeout=" + connectTimeout +
                ", refreshEnabled=" + refreshEnabled +
                '}';
    }
}
