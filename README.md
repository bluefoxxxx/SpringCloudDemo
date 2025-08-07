

# spring-cloud-demo

这是一个基于Spring Cloud的微服务示例项目，展示了如何构建和集成多个微服务组件。项目包含订单服务、用户服务、产品服务、通知服务和API网关。

## 项目结构

- `api-gateway`: 提供统一的API入口和路由功能。
- `order-service`: 管理订单创建、状态更新及与用户和产品服务的交互。
- `user-service`: 提供用户信息管理及缓存处理。
- `product-service`: 提供产品信息管理及缓存处理。
- `notification-service`: 接收消息队列中的通知并进行处理。

## 使用的技术

- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Alibaba Nacos
- RocketMQ
- Redis
- Feign Client
- MyBatis Plus

## 功能特点

- 动态配置刷新（通过Spring Cloud Config）
- 服务发现与注册（Nacos）
- API网关路由与限流
- 分布式事务与消息队列处理
- Redis缓存策略
- Feign远程调用
- 服务降级与重试机制

## 安装与运行

### 前提条件

- JDK 1.8+
- Maven 3.x
- Spring Boot 2.x
- Nacos Server
- RocketMQ Server
- Redis Server

### 启动步骤

1. 启动Nacos服务。
2. 启动RocketMQ服务。
3. 启动Redis服务。
4. 分别进入每个服务模块并运行：

```bash
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

# 启动API网关
cd api-gateway
mvn spring-boot:run
```

## 使用方式

- **订单服务**: 创建订单、更新订单状态、发送毒丸消息。
- **用户服务**: 查询用户信息，使用Redis缓存。
- **产品服务**: 查询产品信息，使用Redis缓存。
- **通知服务**: 处理订单成功或失败的消息，使用Redis记录已处理消息。
- **API网关**: 提供统一入口，实现路由及限流功能。

## 贡献

欢迎提交Pull Request和Issue。请确保提交的代码符合项目的编码规范，并经过测试验证。

## 协议

本项目使用MIT License，请查看具体文件获取详细信息。

## 联系方式

如有问题，请联系作者或在Gitee上提交Issue。

## 致谢

感谢Spring Cloud及Spring Cloud Alibaba社区的支持。