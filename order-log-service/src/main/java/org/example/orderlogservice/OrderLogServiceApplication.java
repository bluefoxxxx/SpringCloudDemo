package org.example.orderlogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OrderLogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderLogServiceApplication.class, args);
    }

}
