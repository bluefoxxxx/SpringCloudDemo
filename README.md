# spring-cloud-demo

这是一个基于Spring Cloud的微服务示例项目，展示了如何构建和集成多个微服务组件。项目包含订单服务、用户服务、产品服务、通知服务、订单日志服务和API网关。

## 项目结构

- `api-gateway`: 提供统一的API入口和路由功能。
    
- `order-service`: 管理订单创建、状态更新及与用户和产品服务的交互。
    
- `user-service`: 提供用户信息管理及缓存处理。
    
- `product-service`: 提供产品信息管理及缓存处理。
    
- `notification-service`: 接收消息队列中的通知并进行处理。
    
- `order-log-service`: 订阅订单状态变更消息并记录日志。
    
- `common-convention`: 通用模块，包含统一的异常处理、返回结果等。
    

## 使用的技术

- **Spring Boot**: 3.x
    
- **Spring Cloud**: 2025.0.0
    
- **Spring Cloud Alibaba**: 2023.0.3.3
    
- **Spring Cloud Gateway**: 提供API网关。
    
- **Nacos**: 用于服务发现和配置管理。
    
- **RocketMQ**: 用于异步消息通信。
    
- **Redis**: 用于缓存。
    
- **Feign Client**: 用于服务间的声明式REST调用。
    
- **MyBatis Plus**: 数据访问层框架。
    
- **Zipkin**: 分布式链路追踪。
    
- **Redisson**: Redis的Java客户端。
    
- **Docker & Docker Compose**: 用于本地环境的容器化部署。
    
- **Kubernetes**: 用于生产环境的容器编排。
    

## 功能特点

- **服务注册与发现**: 所有微服务启动后会注册到Nacos注册中心，并通过Nacos进行服务发现。
    
- **动态配置管理**: 使用Nacos作为配置中心，可以动态刷新应用配置。
    
- **统一API网关**: `api-gateway`作为系统的唯一入口，整合了路由、断言、过滤器和限流等功能。
    
- **服务间通信**: 使用OpenFeign进行服务间的同步调用，并集成了负载均衡。
    
- **分布式链路追踪**: 集成Zipkin，实现微服务间的调用链路追踪。
    
- **缓存策略**:
    
    - **用户服务**: 使用AOP和自定义注解`@CacheLock`，结合Redisson分布式锁，实现了“缓存+数据库”的读写模式，有效防止了缓存击穿和缓存穿透。
        
    - **产品服务**: 实现了手动缓存逻辑，包含缓存空值的防穿透策略。
        
- **消息队列**:
    
    - **RocketMQ**: 用于服务间的解耦和异步通信。
        
    - **普通消息**: `order-service`在订单创建成功后发送普通消息，`notification-service`进行消费。
        
    - **顺序消息**: `order-service`发送顺序消息来保证订单状态的顺序更新，`order-log-service`顺序消费。
        
    - **延迟消息**: `order-service`在下单时发送延迟消息，用于超时未支付的订单自动取消。
        
    - **死信队列**: `notification-service`配置了最大重试次数，消费失败的消息会进入死信队列。
        
    - **消息幂等性**: `notification-service`的消费者使用Redis的Set结构来保证消息处理的幂等性，防止重复消费。
        
    - **毒丸消息**: `order-service`可以发送“毒丸”消息来模拟消费失败的场景，用于测试死信队列和重试机制。
        

## 安装与运行

### 前提条件

- JDK 21
    
- Maven 3.x
    
- Docker 和 Docker Compose
    

### 使用Docker Compose启动

1. **打包项目**: 在项目根目录下，执行以下命令来构建所有微服务的JAR包：
    
    Bash
    
    ```
    mvn clean package
    ```
    
2. **启动所有服务**: 在项目根目录下，`docker-compose.yml`文件已经配置好了所有的基础设施（MySQL, Redis, Nacos, RocketMQ, Zipkin）和应用微服务。执行以下命令一键启动：
    
    Bash
    
    ```
    docker-compose up --build
    ```
    
    该命令会：
    
    - 构建每个微服务的Docker镜像。
        
    - 启动所有在`docker-compose.yml`中定义的服务。
        
    - 使用`depends_on`和`healthcheck`来保证服务的启动顺序和健康状态。
        

### 本地启动 (不使用Docker)

1. **启动基础设施**: 确保你的本地或远程服务器上已经启动了以下服务：
    
    - Nacos Server
        
    - RocketMQ Server
        
    - Redis Server
        
    - MySQL Server，并执行了`init/init.sql`脚本。
        
2. **修改配置**: 根据你的基础设施部署情况，修改各个微服务`src/main/resources/application.yml`中的相关地址。
    
3. **启动微服务**: 分别进入每个服务模块并运行：
    
    Bash
    
    ```
    # 启动用户服务
    cd user-service
    mvn spring-boot:run
    
    # 启动产品服务
    cd product-service
    mvn spring-boot:run
    
    # 启动订单服务
    cd order-service
    mvn spring-boot:run
    
    # 启动通知服务
    cd notification-service
    mvn spring-boot:run
    
    # 启动订单日志服务
    cd order-log-service
    mvn spring-boot:run
    
    # 启动API网关
    cd api-gateway
    mvn spring-boot:run
    ```
    

## 使用方式

### API 端点

所有请求都通过API网关(`http://localhost:8001`)进行路由。

- **获取订单详情**: `GET /api/orders/{id}`
    
    - 聚合了订单信息、用户信息和产品信息。
        
- **创建订单**: `POST /api/orders`
    
    - 请求体:
        
        JSON
        
        ```
        {
          "userId": "1",
          "productId": "2"
        }
        ```
        
- **更新订单状态**: `POST /api/orders/{orderId}/status/{status}`
    
    - 发送顺序消息来更新订单状态。
        
- **获取用户信息**: `GET /api/users/{id}`
    
    - `api-gateway`配置了基于权重的路由规则，90%的流量会路由到v1接口，10%的流量会路由到v2接口并重写路径。
        
- **获取产品信息**: `GET /api/products/{id}`
    
- **发送毒丸消息**: `POST /api/orders/poison-pill`
    
    - 用于测试`notification-service`的死信队列。
        

### Kubernetes 部署

项目提供了Kubernetes的部署文件，位于`/kubernetes`目录下。这些文件包括了各个微服务以及中间件的Deployment和Service配置，可以用于将整个系统部署到Kubernetes集群中。

## 贡献

欢迎提交Pull Request和Issue。请确保提交的代码符合项目的编码规范，并经过测试验证。

## 协议

本项目使用MIT License，请查看具体文件获取详细信息。