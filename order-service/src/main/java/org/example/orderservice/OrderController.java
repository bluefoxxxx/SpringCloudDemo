package org.example.orderservice;


import com.alibaba.cloud.nacos.annotation.NacosConfig;
import org.example.orderservice.config.OrderProperties;
import org.example.orderservice.feign.ProductFeignClient;
import org.example.orderservice.feign.UserFeignClient;
import org.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.Executor;


@RestController
@RefreshScope //动态刷新
public class OrderController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private Executor asyncTaskExecutor;

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

    @NacosConfig(dataId = "order-service-dev.yaml", group = "dev", key = "test.name")
    private String name;
    @GetMapping("/config-test2")
    public String getConfig2() {
        return "Value from Nacos: " + name;
    }

}
