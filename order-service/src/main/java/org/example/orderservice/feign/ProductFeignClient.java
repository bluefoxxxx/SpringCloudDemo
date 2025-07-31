package org.example.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient("product-service")
public interface ProductFeignClient {

    @GetMapping("/products/{id}")
    Map<String, Object> getProductById(@PathVariable("id") Long id);
}
