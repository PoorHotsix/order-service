package com.inkcloud.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSuccessEvent {
    private String paymentId;
    private String orderId;
    private String message;
}
