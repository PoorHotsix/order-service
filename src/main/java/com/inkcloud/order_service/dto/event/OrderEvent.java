package com.inkcloud.order_service.dto.event;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderEvent {
    private String id;
    private OrderEventDto order;
}
