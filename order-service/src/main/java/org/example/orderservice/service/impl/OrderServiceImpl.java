package org.example.orderservice.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import convention.errorcode.BaseErrorCode;
import convention.exception.ClientException;
import convention.exception.ServiceException;
import io.micrometer.context.ContextSnapshot;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.example.orderservice.controller.OrderController;
import org.example.orderservice.dao.entity.OrderDO;
import org.example.orderservice.dao.entity.OrderItemDO;
import org.example.orderservice.dao.mapper.OrderMapper;
import org.example.orderservice.dto.req.CreateOrderReqDTO;
import org.example.orderservice.feign.ProductFeignClient;
import org.example.orderservice.feign.UserFeignClient;
import org.example.orderservice.service.OrderItemService;
import org.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements OrderService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private Executor asyncTaskExecutor;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private static final String ORDER_STATUS_TOPIC = "ORDER_STATUS_TOPIC";

    private static final String ORDER_SUCCESS_TOPIC = "ORDER_SUCCESS_TOPIC";

    private static final String ORDER_TIMEOUT_CANCEL_TOPIC = "ORDER_TIMEOUT_CANCEL_TOPIC"; // 消息延迟主题

    @Override
    public Map<String, Object> getOrderById(Long id) {
        // 1. 从数据库查询订单主信息
        OrderDO order = baseMapper.selectById(id);
        if (order == null) {
            log.warn("查询的订单不存在, 订单ID: {}", id);
            throw new ClientException(BaseErrorCode.CLIENT_ERROR.message(), BaseErrorCode.CLIENT_ERROR);
        }

        // 2. 从数据库查询该订单关联的所有订单项
        List<OrderItemDO> orderItems = orderItemService.selectOrderListsByOrderId(id);

        Long userId = order.getUserId();
        List<Long> productIds = orderItems.stream()
                .map(OrderItemDO::getProductId)
                .toList();
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
                throw new ClientException("用户服务调用失败", e, BaseErrorCode.REMOTE_ERROR);
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
                        throw new ClientException("产品服务调用失败", e, BaseErrorCode.REMOTE_ERROR);
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                }, asyncTaskExecutor))
                .toList();

        //聚合结果
        CompletableFuture.allOf(userFuture, CompletableFuture.allOf(productFutures.toArray(new CompletableFuture[0]))).join();
        Map<String, Object> aggregatedOrder = new HashMap<>();
        aggregatedOrder.put("orderInfo", order);
        try {
            aggregatedOrder.put("user", userFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("聚合订单结果失败", e, BaseErrorCode.SERVICE_ERROR);
        }
        aggregatedOrder.put("products", productFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        return aggregatedOrder;
    }

    @Override
    public void updateOrderStatus(Long orderId, String status) {

        Map<String, Object> messagePayload = Map.of(
                "orderId", orderId,
                "status", status,
                "timestamp", System.currentTimeMillis()
        );

        String messageBody = JSON.toJSONString(messagePayload);

        SendResult sendResult = rocketMQTemplate.syncSendOrderly(
                ORDER_STATUS_TOPIC,
                MessageBuilder.withPayload(messageBody).build(),
                String.valueOf(orderId) // 使用订单ID作为shardingKey
        );

        log.info("顺序消息发送成功! Topic: {}, Body: {}, SendResult: {}",
                ORDER_STATUS_TOPIC, messageBody, sendResult);
    }

    @Override
    @Transactional
    public String createOrder(CreateOrderReqDTO request) {
        OrderDO order = new OrderDO();
        order.setUserId(Long.parseLong(request.getUserId()));
        // 使用 Hutool 生成一个唯一订单号
        String orderNo = IdUtil.simpleUUID();
        order.setOrderNo(orderNo);
        order.setCreateTime(LocalDateTime.now());
        baseMapper.insert(order);

        //创建订单项记录
        OrderItemDO orderItem = new OrderItemDO();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(Long.parseLong(request.getProductId()));
        orderItemService.save(orderItem);

        Long orderId = order.getId();
        log.info("用户 {} 为产品 {} 创建了新订单，订单ID: {}, 订单号: {}", request.getUserId(), request.getProductId(), orderId, orderNo);

        //
        Map<String, Object> payload = Map.of(
                "orderId", String.valueOf(orderId),
                "userId", request.getUserId(),
                "productId", request.getProductId()
        );
        String messageBody = JSON.toJSONString(payload);

        //发送普通消息
        rocketMQTemplate.convertAndSend(ORDER_SUCCESS_TOPIC, messageBody);
        log.info("成功发送下单成功消息到 Topic: {}, Body: {}", ORDER_SUCCESS_TOPIC, messageBody);

        // 发送延迟消息
        Message<String> delayMessage = MessageBuilder.withPayload(messageBody).build();
        // level = 3 10s
        int delayLevel = 3;
        SendResult sendResult = rocketMQTemplate.syncSend(ORDER_TIMEOUT_CANCEL_TOPIC, delayMessage, 3000, delayLevel);

        log.info("成功发送订单超时延迟消息, Topic: {}, Body: {}, DelayLevel: {}, SendResult: {}",
                ORDER_TIMEOUT_CANCEL_TOPIC, messageBody, delayLevel, sendResult);

        // 返回新创建的订单ID
        return String.valueOf(orderId);
    }

    @Override
    public String sendPoisonPillMessage() {

        String topic = "ORDER_SUCCESS_TOPIC";
        // 构造一条包含 "poison": true 的消息
        Map<String, Object> payload = Map.of(
                "orderId", "poison-order-" + System.currentTimeMillis(),
                "userId", "poison-user",
                "poison", true // 毒丸标记
        );
        String messageBody = JSON.toJSONString(payload);

        rocketMQTemplate.convertAndSend(topic, messageBody);

        String logMessage = "已发送“毒丸”消息到 Topic: " + topic;
        log.info(logMessage);
        return logMessage;
    }

}
