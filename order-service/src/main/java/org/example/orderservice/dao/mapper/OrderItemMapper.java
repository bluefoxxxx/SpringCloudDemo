package org.example.orderservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.orderservice.dao.entity.OrderItemDO;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemDO> {
}
