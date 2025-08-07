package org.example.orderservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.orderservice.dao.entity.OrderDO;
import org.example.orderservice.dto.req.CreateOrderReqDTO;

import java.util.Map;

public interface OrderService extends IService<OrderDO> {
    Map<String, Object> getOrderById(Long id);

    /**
     * 更新订单状态并发送顺序消息
     * @param orderId 订单ID
     * @param status 目标状态
     */
    void updateOrderStatus(Long orderId, String status);

    /**
     * 创建订单
     */
    String createOrder(CreateOrderReqDTO request);
}
