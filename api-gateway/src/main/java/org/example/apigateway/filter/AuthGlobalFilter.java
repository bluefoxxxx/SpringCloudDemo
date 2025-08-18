package org.example.apigateway.filter;

import com.alibaba.fastjson2.JSON;
import convention.result.Result;
import convention.result.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

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
            return unauthorizedResponse(response, "A000401", "用户未认证或Token无效");
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

    /**
     * 用于构建并返回一个包含统一Result格式的JSON响应
     */
    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String code, String message) {
        // 1. 设置响应状态码为 401 Unauthorized
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // 2. 设置响应头为 application/json;charset=utf-8
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // 3. 使用 Results 工具类创建失败的 Result 对象
        Result<Void> result = Results.failure(code, message);

        // 4. 将 Result 对象序列化为 JSON 字节数组
        byte[] responseBody = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);

        // 5. 将字节数组包装为 DataBuffer
        DataBuffer buffer = response.bufferFactory().wrap(responseBody);

        // 6. 将 DataBuffer 写入响应体并结束请求
        return response.writeWith(Mono.just(buffer));
    }


    @Override
    public int getOrder() {
        // 设置过滤器的执行顺序，值越小，优先级越高。
        // 安全认证过滤器通常需要最先执行。
        return -1;
    }
}