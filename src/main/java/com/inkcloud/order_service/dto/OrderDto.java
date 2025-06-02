package com.inkcloud.order_service.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.inkcloud.order_service.domain.OrderItem;
import com.inkcloud.order_service.domain.OrderShip;
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
public class OrderDto {
    private String id;
    private OrderState state;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private int price;
    private int quantity;

    private String memberEmail;
    private String memberContact;
    private String memberName;

    private List<OrderItem> orderItems;
    private OrderShip orderShip;

}
