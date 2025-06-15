package com.inkcloud.order_service.dto.event;

import com.inkcloud.order_service.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderEventDto {
    int price;
    int quantity;
    PaymentMethod method;
    String email;
    String paymentId;
    String orderId;
}
