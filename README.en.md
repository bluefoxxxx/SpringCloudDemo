

# spring-cloud-demo

This is a microservices example project based on Spring Cloud, demonstrating how to build and integrate multiple microservices components. The project includes order service, user service, product service, notification service, and an API gateway.

## Project Structure

- `api-gateway`: Provides a unified API entry point and routing functionality.
- `order-service`: Manages order creation, status updates, and interactions with user and product services.
- `user-service`: Provides user information management and caching handling.
- `product-service`: Provides product information management and caching handling.
- `notification-service`: Receives and processes notifications from the message queue.

## Technologies Used

- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Alibaba Nacos
- RocketMQ
- Redis
- Feign Client
- MyBatis Plus

## Features

- Dynamic configuration refresh (via Spring Cloud Config)
- Service discovery and registration (Nacos)
- API gateway routing and rate limiting
- Distributed transactions and message queue processing
- Redis caching strategy
- Feign remote invocation
- Service degradation and retry mechanism

## Installation and Running

### Prerequisites

- JDK 1.8+
- Maven 3.x
- Spring Boot 2.x
- Nacos Server
- RocketMQ Server
- Redis Server

### Startup Steps

1. Start the Nacos service.
2. Start the RocketMQ service.
3. Start the Redis service.
4. Enter each service module and run:

```bash
# Start user service
cd user-service
mvn spring-boot:run

# Start product service
cd product-service
mvn spring-boot:run

# Start order service
cd order-service
mvn spring-boot:run

# Start notification service
cd notification-service
mvn spring-boot:run

# Start API gateway
cd api-gateway
mvn spring-boot:run
```

## How to Use

- **Order Service**: Create orders, update order status, and send poison pill messages.
- **User Service**: Query user information, with Redis caching.
- **Product Service**: Query product information, with Redis caching.
- **Notification Service**: Handle order success or failure messages, using Redis to record processed messages.
- **API Gateway**: Provides a unified entry point, implementing routing and rate limiting.

## Contribution

Pull requests and issues are welcome. Please ensure that the submitted code complies with the project's coding standards and has been tested and verified.

## License

This project uses the MIT License. Please check the corresponding file for full details.

## Contact

If you have any questions, please contact the author or submit an issue on Gitee.

## Acknowledgments

Thanks to the Spring Cloud and Spring Cloud Alibaba communities for their support.