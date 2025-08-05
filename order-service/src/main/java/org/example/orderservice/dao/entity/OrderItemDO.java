package org.example.orderservice.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_order_item")
public class OrderItemDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long productId;
}
