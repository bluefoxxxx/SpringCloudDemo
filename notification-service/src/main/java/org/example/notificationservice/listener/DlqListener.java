package org.example.notificationservice.listener;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 这是一个临时的诊断工具，用于消费和打印死信队列中的消息。
 */
@Service
@RocketMQMessageListener(
        topic = "%DLQ%notification-consumer-group", // 明确订阅DLQ主题
        consumerGroup = "dlq_debug_consumer_group" // 必须使用一个全新的、独立的消费组
)
public class DlqListener implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(DlqListener.class);

    @Override
    public void onMessage(String message) {
        // 将收到的任何消息用非常醒目的格式打印出来
        log.warn("==================== [DLQ DEBUGGER] ====================");
        log.warn("成功从死信队列消费到一条消息: {}", message);
        log.warn("========================================================");
    }
}
