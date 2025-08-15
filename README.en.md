# spring-cloud-demo

This is a microservices example project based on Spring Cloud, demonstrating how to build and integrate multiple microservice components. The project includes an order service, user service, product service, notification service, order-log service, and an API gateway.

## Project Structure

- `api-gateway`: Provides a unified API entry point, routing, and filtering.
    
- `order-service`: Manages order creation, status updates, and interacts with user and product services.
    
- `user-service`: Provides user information management and caching strategies.
    
- `product-service`: Provides product information management and caching strategies.
    
- `notification-service`: Receives and processes notifications from the message queue.
    
- `order-log-service`: Subscribes to order status changes and logs them sequentially.
    
- `common-convention`: A common module containing unified response formats, exception handling, and error codes.
    

## Technologies Used

- **Spring Boot**: 3.x
    
- **Spring Cloud**: 2025.0.0
    
- **Spring Cloud Alibaba**: 2023.0.3.3
    
- **Spring Cloud Gateway**: For the API Gateway.
    
- **Nacos**: For service discovery and distributed configuration.
    
- **RocketMQ**: For asynchronous messaging.
    
- **Redis**: For caching.
    
- **Feign Client**: For declarative REST API calls between services.
    
- **MyBatis Plus**: As the data access framework.
    
- **Zipkin**: For distributed tracing.
    
- **Redisson**: As an advanced Redis client for distributed locks.
    
- **Docker & Docker Compose**: For containerizing and running the application stack locally.
    
- **Kubernetes**: Manifest files are provided for production-like deployments.
    

## Features

- **Service Discovery and Registration**: All microservices register with Nacos and discover each other via their service names.
    
- **Dynamic Configuration**: Uses Nacos for centralized configuration management with support for dynamic refreshes.
    
- **Unified API Gateway**: The `api-gateway` serves as the single entry point, handling routing, predicate-based matching, filtering, and rate limiting.
    
- **Interservice Communication**: Employs OpenFeign for synchronous, load-balanced communication between services.
    
- **Distributed Tracing**: Integrates with Zipkin to trace requests across microservice boundaries.
    
- **Caching Strategies**:
    
    - **User Service**: Implements an advanced caching pattern using AOP and a custom `@CacheLock` annotation. It combines a cache-aside strategy with Redisson distributed locks to prevent cache breakdown and penetration.
        
    - **Product Service**: Implements a manual caching strategy, including caching null values to prevent cache penetration.
        
- **Message Queue**:
    
    - **RocketMQ**: Used for decoupling services and asynchronous communication.
        
    - **Normal Messages**: The `order-service` sends a message upon successful order creation, which is consumed by the `notification-service`.
        
    - **Sequential Messages**: The `order-service` sends messages sequentially to guarantee the order of status updates, which are consumed sequentially by the `order-log-service`.
        
    - **Delayed Messages**: When an order is created, the `order-service` sends a delayed message to handle automatic order cancellation for unpaid orders.
        
    - **Dead-Letter Queue (DLQ)**: The `notification-service` is configured with a maximum number of retries. If a message fails consumption repeatedly, it is sent to a DLQ.
        
    - **Message Idempotency**: The consumer in the `notification-service` uses a Redis Set to store processed message IDs, ensuring that each message is processed only once.
        
    - **Poison Pill Messages**: The `order-service` includes an endpoint to send "poison pill" messages to simulate consumption failures, allowing for testing of the retry and DLQ mechanisms.
        

## Installation and Running

### Prerequisites

- JDK 21
    
- Maven 3.x
    
- Docker and Docker Compose
    

### Running with Docker Compose

1. **Package the Project**: From the project root, run the following command to build the JAR files for all microservices:
    
    Bash
    
    ```
    mvn clean package
    ```
    
2. **Start All Services**: The `docker-compose.yml` file in the root directory is pre-configured with all infrastructure (MySQL, Redis, Nacos, RocketMQ, Zipkin) and application microservices. Run the following command to start everything:
    
    Bash
    
    ```
    docker-compose up --build
    ```
    
    This command will:
    
    - Build the Docker image for each microservice.
        
    - Start all services defined in `docker-compose.yml`.
        
    - Use `depends_on` and `healthcheck` to manage service startup order and health.
        

### Running Locally (Without Docker)

1. **Start Infrastructure**: Ensure you have the following services running on your local machine or a remote server:
    
    - Nacos Server
        
    - RocketMQ Server
        
    - Redis Server
        
    - MySQL Server (after running the `init/init.sql` script).
        
2. **Update Configurations**: Modify the service addresses in each microservice's `src/main/resources/application.yml` file to match your infrastructure setup.
    
3. **Start Microservices**: Navigate to each service's directory and run it:
    
    Bash
    
    ```
    # Start User Service
    cd user-service
    mvn spring-boot:run
    
    # Start Product Service
    cd product-service
    mvn spring-boot:run
    
    # Start Order Service
    cd order-service
    mvn spring-boot:run
    
    # Start Notification Service
    cd notification-service
    mvn spring-boot:run
    
    # Start Order-Log Service
    cd order-log-service
    mvn spring-boot:run
    
    # Start API Gateway
    cd api-gateway
    mvn spring-boot:run
    ```
    

## How to Use

### API Endpoints

All requests are routed through the API Gateway (`http://localhost:8001`).

- **Get Order Details**: `GET /api/orders/{id}`
    
    - Aggregates order info, user info, and product info.
        
- **Create an Order**: `POST /api/orders`
    
    - Request Body:
        
        JSON
        
        ```
        {
          "userId": "1",
          "productId": "2"
        }
        ```
        
- **Update Order Status**: `POST /api/orders/{orderId}/status/{status}`
    
    - Sends a sequential message to update the order status.
        
- **Get User Info**: `GET /api/users/{id}`
    
    - The API Gateway is configured with weight-based routing: 90% of traffic is routed to the v1 endpoint, and 10% is routed to the v2 endpoint with a path rewrite.
        
- **Get Product Info**: `GET /products/{id}`
    
- **Send a Poison Pill Message**: `POST /orders/poison-pill`
    
    - Used to test the dead-letter queue in the `notification-service`.
        

### Kubernetes Deployment

The project includes Kubernetes manifest files in the `/kubernetes` directory. These files contain Deployment and Service configurations for each microservice and the required middleware, allowing you to deploy the entire system to a Kubernetes cluster.

## Contribution

Pull requests and issues are welcome. Please ensure that any submitted code adheres to the project's coding standards and has been thoroughly tested.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.