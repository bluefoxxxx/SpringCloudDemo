package org.example.orderservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "feign1.client.config.default")
@RefreshScope
public class OrderProperties {
    private int connectTimeout;
    private int readTimeout;
}
