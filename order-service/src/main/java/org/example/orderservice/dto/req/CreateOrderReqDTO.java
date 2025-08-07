package org.example.orderservice.dto.req;

import lombok.Data;

@Data
public class CreateOrderReqDTO {
    private String orderId; // 手动传入 orderId 以方便验证重复消费
    private String userId;
    private String productId;
}
