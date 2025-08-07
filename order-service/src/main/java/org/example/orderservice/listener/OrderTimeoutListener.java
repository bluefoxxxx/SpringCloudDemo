package org.example.orderservice.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(
        topic = "ORDER_TIMEOUT_CANCEL_TOPIC", // 订阅延迟消息的主题
        consumerGroup = "order-timeout-consumer-group"
)
public class OrderTimeoutListener implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutListener.class);

    @Override
    public void onMessage(String message) {
        log.info("【延迟消息】消费者收到订单超时消息: {}", message);

        JSONObject messageBody = JSON.parseObject(message);
        String orderId = messageBody.getString("orderId");

        // 检查数据库中订单的支付状态
        log.info("...开始检查订单 {} 的支付状态...", orderId);
        boolean isPaid = checkPaymentStatus(orderId);

        // 如果未支付，则执行取消订单的逻辑
        if (!isPaid) {
            log.warn("订单 {} 超过10秒仍未支付，系统将自动执行取消订单操作！", orderId);
            // TODO 更新数据库中订单状态为“已取消”的业务逻辑
        } else {
            log.info("订单 {} 已支付，无需处理。", orderId);
        }
    }

    // 模拟查询数据库以检查订单支付状态
    private boolean checkPaymentStatus(String orderId) {
        // TODO 真实业务中，查询数据库
        return false;
    }
}
