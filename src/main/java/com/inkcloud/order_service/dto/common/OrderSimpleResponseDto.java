package com.inkcloud.order_service.dto.common;

import java.time.LocalDateTime;

import com.inkcloud.order_service.enums.OrderState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderSimpleResponseDto {
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OrderState state;
}
