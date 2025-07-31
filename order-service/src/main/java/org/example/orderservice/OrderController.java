package org.example.orderservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderController {

    @GetMapping("/orders/{id}")
    public Map<String, Object> getOrderById(@PathVariable Long id) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        order.put("userId", 1L);
        order.put("productIds", Arrays.asList(101L, 102L));
        return order;
    }
}
