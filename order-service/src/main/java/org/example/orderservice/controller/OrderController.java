package org.example.orderservice.controller;


import org.example.orderservice.config.OrderProperties;
import org.example.orderservice.convention.result.Result;
import org.example.orderservice.convention.result.Results;
import org.example.orderservice.dto.req.CreateOrderReqDTO;
import org.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RefreshScope //动态刷新
public class OrderController {

    @Autowired
    private OrderProperties orderProperties;

    @Autowired
    private OrderService orderService;



    private static final Logger log = LoggerFactory.getLogger(OrderController.class);


    @GetMapping("/orders/{id}")
    public Result<Map<String, Object>> getOrderById(@PathVariable("id") Long id){
        return Results.success(orderService.getOrderById(id));
    }

    @GetMapping("/config-test")
    public Result<String> getConfig() {
        String configInfo = "Current Feign Connect Timeout: " + orderProperties.getConnectTimeout() + "Current Read Timeout: " + orderProperties.getReadTimeout();
        log.info(configInfo); // 验证日志级别刷新
        return Results.success(configInfo);
    }

    @PostMapping("/orders/{orderId}/status/{status}")
    public Result<String> updateOrderStatus(@PathVariable("orderId") Long orderId, @PathVariable("status") String status) {
        orderService.updateOrderStatus(orderId, status);
        return Results.success("Status update message for order " + orderId + " has been sent.");
    }

    @PostMapping("/orders")
    public Result<String> createOrder(@RequestBody CreateOrderReqDTO request) {
        String orderId = orderService.createOrder(request);
        return Results.success("Order created successfully with ID: " + orderId);
    }

    // 专门发送毒丸消息
    @PostMapping("/orders/poison-pill")
    public Result<String> sendPoisonPillMessage() {
        return Results.success(orderService.sendPoisonPillMessage());
    }
}
