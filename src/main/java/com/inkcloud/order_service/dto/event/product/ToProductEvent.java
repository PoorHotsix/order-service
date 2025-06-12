package com.inkcloud.order_service.dto.event.product;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToProductEvent {
    String orderId;
    List<ToProductEventDto> dtos;
}
