package com.inkcloud.order_service.dto.event.stat;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToStatEvent {
    String orderId;
    int totalQuantity;
    int totalSales; // 총 주문 금액
    LocalDateTime createdAt;
}
