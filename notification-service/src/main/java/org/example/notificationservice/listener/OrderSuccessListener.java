package org.example.notificationservice.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(
        topic = "ORDER_SUCCESS_TOPIC", // 订阅下单成功主题
        consumerGroup = "notification-consumer-group"
)
public class OrderSuccessListener implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(OrderSuccessListener.class);
    private static final String PROCESSED_SET_KEY = "processed:coupons:set";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void onMessage(String message) {
        log.info("接收到下单成功消息: {}", message);

        // 解析消息，获取订单ID作为唯一业务ID
        JSONObject messageBody = JSON.parseObject(message);
        String orderId = messageBody.getString("orderId");

        // 如果元素是第一次加入，返回 1；如果元素已存在，返回 0
        boolean isFirstTime = redisTemplate.opsForSet().add(PROCESSED_SET_KEY, orderId) == 1L;

        if (isFirstTime) {
            // 第一次处理，执行发券业务
            log.info("【幂等性检查通过】订单ID: {} 是首次处理，开始发放优惠券...", orderId);
            // 模拟发券操作
            try {
                Thread.sleep(1000); // 模拟业务耗时
            } catch (InterruptedException e) {
                // ignore
            }
            log.info("...订单ID: {} 的优惠券发放成功！", orderId);
        } else {
            // 重复消息，直接忽略
            log.warn("【幂等性检查拒绝】订单ID: {} 是重复消息，已处理过，不再发放优惠券。", orderId);
        }
    }
}
