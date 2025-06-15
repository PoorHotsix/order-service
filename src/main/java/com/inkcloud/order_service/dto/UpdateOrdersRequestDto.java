package com.inkcloud.order_service.dto;

import java.util.List;

import com.inkcloud.order_service.enums.OrderState;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateOrdersRequestDto {
    List<String> orderIds;
    OrderState state;

}
