package com.inkcloud.order_service.dto;

import java.util.List;

import com.inkcloud.order_service.dto.child.OrderItemDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderReviewDto {
    private List<OrderItemDto> orderItems; // ㅇ
}
