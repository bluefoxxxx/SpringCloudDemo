package org.example.orderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.orderservice.dao.entity.OrderItemDO;
import org.example.orderservice.dao.mapper.OrderItemMapper;
import org.example.orderservice.service.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemDO> implements OrderItemService {
    @Override
    public List<OrderItemDO> selectOrderListsByOrderId(Long id) {
        LambdaQueryWrapper<OrderItemDO> queryWrapper = Wrappers.lambdaQuery(OrderItemDO.class)
                .eq(OrderItemDO::getOrderId, id);
        return baseMapper.selectList(queryWrapper);
    }
}
