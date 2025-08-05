package org.example.orderservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.orderservice.dao.entity.OrderDO;

@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {
}
