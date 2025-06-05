package com.inkcloud.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStartEventDto {
    int price;
    int quantity;
    String email;
    String paymentId;
    String orderId;
}
