package org.example.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient("user-service")
public interface UserFeignClient {

    @GetMapping("/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") Long id);
}
