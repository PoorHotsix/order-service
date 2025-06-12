package com.inkcloud.order_service.dto.event.bestseller;

import java.util.List;

import com.inkcloud.order_service.dto.child.OrderItemDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToBestSellerEvent {
    List<OrderItemDto> dtos;
}
