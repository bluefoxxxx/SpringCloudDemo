package org.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String VALID_TOKEN = "admin-token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        log.info("AuthGlobalFilter running... inspecting token: {}", token);

        // 1. 检查 Token 是否存在或有效
        if (token == null || !VALID_TOKEN.equals(token)) {
            log.warn("Authentication failed! Token is missing or invalid.");
            // 2. Token 无效，直接拒绝请求
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // 设置 401 未授权状态码
            return response.setComplete(); // 结束请求处理
        }

        // 3. Token 验证通过
        log.info("Authentication successful. Adding user role to header.");
        // 4. 在请求头中添加解析后的用户信息，并创建新的请求对象
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Role", "admin")
                .build();

        // 5. 使用新的请求对象创建一个新的 ServerWebExchange
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // 6. 将新的 Exchange 传递给过滤器链的下一个过滤器
        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        // 设置过滤器的执行顺序，值越小，优先级越高。
        // 安全认证过滤器通常需要最先执行。
        return -1;
    }
}