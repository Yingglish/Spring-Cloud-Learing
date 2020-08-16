<<<<<<< Updated upstream
# Spring-Cloud-Learing
=======
# Spring-Cloud-Learning 
>>>>>>> Stashed changes

## Eureka作为服务注册发现中心
> `Eureka` 是一个 **高可用** 的组件，它没有 **后端缓存**。每一个 **实例** 注册之后，需要 **定时** 向 **注册中心** 发送 **心跳**

## 搭建Eureka注册中心

把注解 `@EnableEurekaServer` 加在 `Spring Boot` 工程的启动类 `Application` 上面。在默认情况下 `Eureka Server` 也是一个 `Eureka Client`，必须要指定一个 `Server`

```yaml
server:
  port: 8761 # 指定运行端口

eureka:
  instance:
    hostname: localhost # 指定服务名称
  client:
    registerWithEureka: false # 指定是否要从注册中心获取服务（注册中心不需要开启）
    fetchRegistry: false # 指定是否要注册到注册中心（注册中心不需要开启）
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/

# Eureka-server 默认的模板引擎为Freemarker 若要使用Thymeleaf则需要填入如下设置
spring:
  freemarker:
    template-loader-path: classpath:/templates/
    prefer-file-system-access: false
```

- **eureka.client.register-with-eureka** ：设置是否将自己作为 `Eureka Client` 注册到 `Eureka Server`，默认为 `true`

- **eureka.client.fetch-registry **：设置是否从 `Eureka Server` 获取 **注册信息**，默认为 `true`。因为本例是一个 **单点** 的 `Eureka Server`，不需要 **同步** 其他 `Eureka Server` 节点的数据，所以设置为 `false`

- **eureka.client.service-url.defaultZone **：设置与 `Eureka Server` 的 **交互地址**，**查询** 和 **注册服务** 都依赖这个地址，如果有多个可以使用 **英文逗号分隔**

### 创建Eureka服务提供者

> - 当一个 `Eureka Client` 向 `Eureka Server` 发起 **注册** 时，它会提供一些 **元数据**，例如 **主机** 和 **端口** 等等
>
> - `Eureka Server` 从每个 `Eureka Client` 实例接收 **心跳消息**。 如果 **心跳超时**，则通常将该实例从 `Eureka Server` 中删除

通过 **注解** `@EnableEurekaClient` 表明自己是一个 `Eureka Client`，并在在 **配置文件** 中注明的 **服务注册中心** 的地址

```yaml
server:
  port: 8101
spring:
  application:
    name: eureka-client
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

> `Eureka` **客户端** 需要指明 `spring.application.name`，作为服务的 **唯一标识**，**服务之间** 相互调用会基于这个 `name`

### Eureka的常用配置
```yaml
eureka:
  client: #eureka客户端配置
    register-with-eureka: true #是否将自己注册到eureka服务端上去
    fetch-registry: true #是否获取eureka服务端上注册的服务列表
    service-url:
      defaultZone: http://localhost:8001/eureka/ # 指定注册中心地址
    enabled: true # 启用eureka客户端
    registry-fetch-interval-seconds: 30 #定义去eureka服务端获取服务列表的时间间隔
  instance: #eureka客户端实例配置
    lease-renewal-interval-in-seconds: 30 #定义服务多久去注册中心续约
    lease-expiration-duration-in-seconds: 90 #定义服务多久不去续约认为服务失效
    metadata-map:
      zone: jiangsu #所在区域
    hostname: localhost #服务主机名称
    prefer-ip-address: false #是否优先使用ip来作为主机名
  server: #eureka服务端配置
    enable-self-preservation: false #关闭eureka服务端的保护机制

```

### 搭建Eureka注册中心集群
> 由于所有服务都会注册到注册中心去，服务之间的调用都是通过从注册中心获取的服务列表来调用,注册中心一旦宕机，所有服务调用都会出现问题。所以需要多个注册中心组成集群来提供服务

给`eureka-sever`添加配置文件`application-cluster1.yml`和`application-cluster2.yml`
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
> 使用域名须在在host文件中进行配置
 ```
 127.0.0.1 cluster1
 127.0.0.1 cluster2
 ```

在idea添加 cluster1 和 cluster 2俩种启动配置并启动，访问 http://cluster1:8002/ 这个注册中心，若在`DS Replicas`区域将会出现
cluster2，说明cluster2成为cluster1的备份

在`eureka-client`添加配置文件`application-master.yml`，让其连接到集群
```yaml
server:
  port: 8101
spring:
  application:
    name: eureka-client
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://cluster1:8002/eureka/,http://cluster2:8003/eureka/ #同时注册到两个注册中心
```
> 以该配置文件启动后访问任意一个注册中心节点都可以看到`eureka-client`

## 负载均衡的服务调用

### Ribbon的常用配置

#### 全局配置

```yaml
ribbon:
  ConnectTimeout: 1000 #服务请求连接超时时间（毫秒）
  ReadTimeout: 3000 #服务请求处理超时时间（毫秒）
  OkToRetryOnAllOperations: true #对超时请求启用重试机制
  MaxAutoRetriesNextServer: 1 #切换重试实例的最大个数
  MaxAutoRetries: 1 # 切换实例后重试最大次数
  NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule #修改负载均衡算法
```

<<<<<<< Updated upstream
在idea添加 cluster1 和 cluster 2俩种启动配置
=======
#### 指定服务进行配置

```yaml
eureka-client:
  ribbon:
    ConnectTimeout: 1000 #服务请求连接超时时间（毫秒）
    ReadTimeout: 3000 #服务请求处理超时时间（毫秒）
    OkToRetryOnAllOperations: true #对超时请求启用重试机制
    MaxAutoRetriesNextServer: 1 #切换重试实例的最大个数
    MaxAutoRetries: 1 # 切换实例后重试最大次数
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule #修改负载均衡算法
```

### Ribbon的负载均衡策略

> 就是当A服务调用B服务时，此时B服务有多个实例，这时A服务以何种方式来选择调用的B实例

- `RandomRule`：从提供服务的实例中以随机的方式；
- `RoundRobinRule`：以线性轮询的方式，就是维护一个计数器，从提供服务的实例中按顺序选取，第一次选第一个，第二次选第二个，以此类推，到最后一个以后再从头来过；
- `RetryRule`：在RoundRobinRule的基础上添加重试机制，即在指定的重试时间内，反复使用线性轮询策略来选择可用实例；
- `WeightedResponseTimeRule`：对RoundRobinRule的扩展，响应速度越快的实例选择权重越大，越容易被选择；
- `BestAvailableRule`：选择并发较小的实例；
- `AvailabilityFilteringRule`：先过滤掉故障实例，再选择并发较小的实例；
- `ZoneAwareLoadBalancer`：采用双重过滤，同时过滤不是同一区域的实例和故障实例，选择并发较小的实例。
>>>>>>> Stashed changes

