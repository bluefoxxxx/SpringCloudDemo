package org.example.orderlogservice.listener;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(
        topic = "ORDER_STATUS_TOPIC",
        consumerGroup = "order-log-consumer-group",
        consumeMode = ConsumeMode.ORDERLY // 设置为顺序消费模式
)
public class OrderStatusListener implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusListener.class);

    @Override
    public void onMessage(String message) {
        log.info("【顺序消费】接收到订单状态变更消息 -> {}", message);
    }
}
