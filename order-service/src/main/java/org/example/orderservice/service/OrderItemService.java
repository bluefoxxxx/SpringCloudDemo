package org.example.orderservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.orderservice.dao.entity.OrderItemDO;

import java.util.List;

public interface OrderItemService extends IService<OrderItemDO> {
    List<OrderItemDO> selectOrderListsByOrderId(Long id);
}
