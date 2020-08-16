"# Spring-Cloud-Learing" 

## 搭建Eureka注册中心集群
给eureka-sever添加配置文件application-cluster1.yml和application-cluster2.yml
来搭建俩个注册中心
```yaml
server:
  port: 8002
spring:
  application:
    name: eureka-server
eureka:
  instance:
    hostname: cluster1
  client:
    serviceUrl:
      defaultZone: http://cluster2:8003/eureka/ #注册到另一个Eureka注册中心
    fetch-registry: true
    register-with-eureka: true
```
```yaml
server:
  port: 8003
spring:
  application:
    name: eureka-server
eureka:
  instance:
    hostname: cluster2
  client:
    serviceUrl:
      defaultZone: http://cluster1:8002/eureka/ #注册到另一个Eureka注册中心
    fetch-registry: true
    register-with-eureka: true
```
> 如果要使用域名可在host文件中进行配置
 ```
 127.0.0.1 cluster1
 127.0.0.1 cluster2
```

在idea添加 cluster1 和 cluster 2俩种启动配置
