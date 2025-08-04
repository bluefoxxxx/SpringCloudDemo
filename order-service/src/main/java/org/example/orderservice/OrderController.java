package org.example.orderservice;


import com.alibaba.cloud.nacos.annotation.NacosConfig;
import io.micrometer.context.ContextSnapshot;
import org.example.orderservice.config.OrderProperties;
import org.example.orderservice.feign.ProductFeignClient;
import org.example.orderservice.feign.UserFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


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

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);


    @GetMapping("/orders/{id}")
    public Map<String, Object> getOrderById(@PathVariable("id") Long id) throws ExecutionException, InterruptedException {
        //订单基本信息
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        Long userId = 1L;
        List<Long> productIds = Arrays.asList(101L, 102L);
        order.put("userId", userId);
        order.put("productIds", productIds);

        // 1. 在主线程中，捕获所有上下文
        // 1.1 捕获 Micrometer 能识别的所有上下文
        ContextSnapshot snapshot = ContextSnapshot.captureAll();
        // 1.2 手动捕获被 Micrometer 漏掉的 Web 请求上下文
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();


        // 2. 在异步调用中，组合使用自动和手动恢复
        CompletableFuture<Map<String, Object>> userFuture = CompletableFuture.supplyAsync(() -> {
            // 2.1 手动设置和清理 RequestContextHolder
            RequestContextHolder.setRequestAttributes(requestAttributes);
            try {
                // 2.2 使用 snapshot.wrap() 自动处理 Micrometer 相关的上下文
                Callable<Map<String, Object>> userCallable = () -> userFeignClient.getUserById(userId);
                Callable<Map<String, Object>> wrappedUserCallable = snapshot.wrap(userCallable);
                return wrappedUserCallable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                // 2.3 手动清理 RequestContextHolder
                RequestContextHolder.resetRequestAttributes();
            }
        }, asyncTaskExecutor);

        // 对 product 的调用也应用相同的逻辑
        List<CompletableFuture<Map<String, Object>>> productFutures = productIds.stream()
                .map(productId -> CompletableFuture.supplyAsync(() -> {
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    try {
                        Callable<Map<String, Object>> productCallable = () -> productFeignClient.getProductById(productId);
                        Callable<Map<String, Object>> wrappedProductCallable = snapshot.wrap(productCallable);
                        return wrappedProductCallable.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                }, asyncTaskExecutor))
                .toList();

        //聚合结果
        CompletableFuture.allOf(userFuture, CompletableFuture.allOf(productFutures.toArray(new CompletableFuture[0]))).join();
        Map<String, Object> aggregatedOrder = new HashMap<>(order);
        aggregatedOrder.put("user", userFuture.get());
        aggregatedOrder.put("products", productFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        return aggregatedOrder;
    }

    @GetMapping("/config-test")
    public String getConfig() {
        String configInfo = "Current Feign Connect Timeout: " + orderProperties.getConnectTimeout() + "Current Read Timeout: " + orderProperties.getReadTimeout();
        log.info(configInfo); // 验证日志级别刷新
        return configInfo;
    }
//    @Autowired
//    private Environment environment;
//
//    @GetMapping("/config-test")
//    public String getConfig() {
//        // 直接从环境中读取最简单的配置
//        String testName = environment.getProperty("test.name");
//        return "Value from Nacos: " + testName;
//    }

    @NacosConfig(dataId = "order-service-dev.yaml", group = "dev", key = "test.name")
    private String name;
    @GetMapping("/config-test2")
    public String getConfig2() {
        return "Value from Nacos: " + name;
    }

    @Value("${feign1.client.config.default.connectTimeout}")
    private int c1;

    @Value("${feign1.client.config.default.readTimeout}")
    private int c2;

    @Value("${logging.level.org.example.orderservice}")
    private String c3;

    @Value("${database.url}")
    private String c4;

    @GetMapping("/config-test1")
    public String getConfig1() {
        String configInfo = "Current Feign Connect Timeout: " + c1 + " Current Read Timeout: " + c2 + " Log level: " + c3 + " "+ c4;
        log.info(configInfo); // 验证日志级别刷新
        return configInfo;
    }

}
