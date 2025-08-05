package org.example.orderservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.orderservice.dao.entity.OrderDO;

import java.util.Map;

public interface OrderService extends IService<OrderDO> {
    Map<String, Object> getOrderById(Long id);
}
