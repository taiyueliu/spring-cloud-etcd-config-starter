# spring-cloud-etcd-config-starter

此项目是基于ETCD开发的spring cloud config

## 使用

1、下载项目install到maven仓库

2、在项目中引用

```xml
<dependency>
    <groupId>com.nyquistdata</groupId>
    <artifactId>spring-cloud-config-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

3、bootstrap.yaml配置

```
spring:
  profiles:
    active: dev
  cloud:
    etcd:
      config:
      	username: test #etcd的账号
      	password: 123 #etcd的密码
      	namespcase: dev #etcd的namespcase
        server-addr: http://localhost:2379,http://localhost:2479 #etcd的集群地址
        prefix: my-key-prefix #在etcd中key的前缀
        file-extension: yaml #文件后缀
```

此配置在会在etcd中查找key为my-key-prefix，my-key-prefix.yaml，my-key-prefix-dev.yaml的value;这里的value格式化要与配置的file-extension一致，不然会解析不出来；file-extension可以是properties,yml,yaml,json,xml的格式

成为key前缀的优先级（与nacos config一致）:

spring.cloud.etcd.config.perfix > spring.cloud.etcd.config.name > spring.application.name

