package com.inkcloud.order_service.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inkcloud.order_service.dto.child.MemberDto;
import com.inkcloud.order_service.dto.child.OrderItemDto;
import com.inkcloud.order_service.dto.child.OrderShipDto;
import com.inkcloud.order_service.dto.child.PaymentDto;
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id; // ㅇ
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private OrderState state; // ㅇ
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt; // ㅇ
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private PaymentDto paymentDto;
    
    private int price; // ㅇ
    private int quantity; // ㅇ 
    
    private MemberDto member; // ㅇ 

    private List<OrderItemDto> orderItems; // ㅇ
    private OrderShipDto orderShip; // ㅇ
}
