package org.example.orderservice.dto.req;

import lombok.Data;

@Data
public class CreateOrderReqDTO {
    private String userId;
    private String productId;
}
