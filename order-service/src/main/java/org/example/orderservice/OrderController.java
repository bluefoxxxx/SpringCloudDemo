package org.example.orderservice;


import org.example.orderservice.config.OrderProperties;
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
    public Map<String, Object> getOrderById(@PathVariable("id") Long id){
        return orderService.getOrderById(id);
    }

    @GetMapping("/config-test")
    public String getConfig() {
        String configInfo = "Current Feign Connect Timeout: " + orderProperties.getConnectTimeout() + "Current Read Timeout: " + orderProperties.getReadTimeout();
        log.info(configInfo); // 验证日志级别刷新
        return configInfo;
    }

    @PostMapping("/orders/{orderId}/status/{status}")
    public String updateOrderStatus(@PathVariable("orderId") Long orderId, @PathVariable("status") String status) {
        orderService.updateOrderStatus(orderId, status);
        return "Status update message for order " + orderId + " has been sent.";
    }

    @PostMapping("/orders")
    public String createOrder(@RequestBody CreateOrderReqDTO request) {
        String orderId = orderService.createOrder(request);
        return "Order created successfully with ID: " + orderId;
    }

    // 专门发送毒丸消息
    @PostMapping("/orders/poison-pill")
    public String sendPoisonPillMessage() {
        return orderService.sendPoisonPillMessage();
    }
}
