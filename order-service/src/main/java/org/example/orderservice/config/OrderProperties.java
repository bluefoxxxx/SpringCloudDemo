package org.example.orderservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "feign1.client.config.default")
public class OrderProperties {
    private int connectTimeout;
    private int readTimeout;
}
